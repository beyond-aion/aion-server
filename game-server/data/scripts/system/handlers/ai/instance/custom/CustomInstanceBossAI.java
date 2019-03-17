package ai.instance.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.custom.instance.CustomInstanceService;
import com.aionemu.gameserver.custom.instance.neuralnetwork.PlayerModel;
import com.aionemu.gameserver.custom.instance.neuralnetwork.PlayerModelController;
import com.aionemu.gameserver.custom.instance.neuralnetwork.PlayerModelEntry;
import com.aionemu.gameserver.dataholders.loadingutils.adapters.NpcEquipmentList;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.SkillElement;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.functions.StatSetFunction;
import com.aionemu.gameserver.model.stats.container.PlayerGameStats;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.EquipType;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.condition.Condition;
import com.aionemu.gameserver.skillengine.condition.DpCondition;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.Skill.SkillMethod;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.model.SkillType;
import com.aionemu.gameserver.skillengine.properties.Properties.CastState;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author Jo
 */
@AIName("custom_instance_boss")
public class CustomInstanceBossAI extends GeneralNpcAI {

	private final static List<Integer> restrictedSkills = Arrays.asList(243, 244, 277, 282, 302, 912, 1178, 1346, 1347, 1757, 2106, 2167, 2400, 2425,
		2565, 3331, 3663, 3705, 3729, 3683, 3788, 3789, 3835, 3837, 3643, 3839, 3833, 3991, 4407, 2778, 2425, 8291, 10164, 11011, 13010, 13234, 13231);

	private Player player;
	private PlayerModel model;
	private Future<?> skillTask, castTimeout;
	private int previousSkill, rank;

	private List<Integer> skillSet;
	private boolean onlyAttack;

	public CustomInstanceBossAI(Npc owner) {
		super(owner);
	}

	@Override
	public int modifyDamage(Creature attacker, int damage, Effect effect) {
		return Math.round(damage * 0.42f); // pseudo PvP reduce
	}

	@Override
	public int modifyOwnerDamage(int damage, Creature effected, Effect effect) {
		return Math.round(damage * 0.58f); // pseudo PvP reduce
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		previousSkill = -1;
		player = getOwner().getPosition().getWorldMapInstance().getPlayersInside().get(0);

		if (player.getPlayerClass() == PlayerClass.RIDER)
			onlyAttack = true;
		else {
			// model player behavior
			skillSet = PlayerModelController.getSkillSetForPlayer(player.getObjectId());
			model = PlayerModelController.trainModelForPlayer(player, skillSet);
			onlyAttack = false;
		}
		// adapt player parameters
		rank = CustomInstanceService.getInstance().getPlayerRankObject(player.getObjectId()).getRank();
		adaptAppearance();
		adaptStats();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();

		// prevent reset-abusing
		if (getOwner().getSkillCoolDowns() != null)
			getOwner().getSkillCoolDowns().clear();
	}

	@Override
	public void handleCreatureDetected(Creature creature) {
		super.handleCreatureDetected(creature);

		if (PositionUtil.getDistance(getPosition().getX(), getPosition().getY(), getPosition().getZ(), creature.getX(), creature.getY(),
			creature.getZ()) <= 45 && creature == player) {
			getOwner().getAggroList().addHate(creature, 100); // early aggro

			if (skillTask == null && !onlyAttack)
				skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::checkSkillRotation, 200, 200);
		}
	}

	private void checkSkillRotation() {
		if (!getOwner().isCasting()) {
			int skillID = getPlayerSkill();

			if (skillID != -1 && castTimeout == null) {
				Skill skill = SkillEngine.getInstance().getSkill(getOwner(), skillID, 1, getTarget());
				skill.useSkill();

				// workaround for slide-cast bug:
				getEffectController().setAbnormal(AbnormalState.SANCTUARY);
				long castDuration = (long) (skill.getSkillTemplate().getDuration() + (0.3f * getOwner().getGameStats().getAttackSpeed().getCurrent()));
				if (skill.getSkillTemplate().getCooldown() == 0) // item skills
					castDuration = 0;

				castTimeout = ThreadPoolManager.getInstance().schedule(() -> {
					getEffectController().unsetAbnormal(AbnormalState.SANCTUARY);
					if (getOwner().getCastingSkill() != null)
						getOwner().getCastingSkill().cancelCast();

					// little break after timeout (for walking)
					castTimeout = ThreadPoolManager.getInstance().schedule(() -> {
						castTimeout = null;
					}, 1000);
				}, castDuration); // after cast / instant skill: 300ms * attack speed
			}
		}
	}

	private int getPlayerSkill() {
		if (getTarget() == null || !(getTarget() instanceof Creature))
			return -1;

		// remove shock
		if ((getEffectController().isAbnormalSet(AbnormalState.ANY_STUN) || getEffectController().isAbnormalSet(AbnormalState.OPENAERIAL))
			&& getOwner().getSkillCoolDown(283) <= System.currentTimeMillis())
			return 283;

		// assess game state:
		double[] inputArray = new PlayerModelEntry(getOwner(), -1, player).toStateInputArray(skillSet, previousSkill);

		// compute skill selection:
		if (model != null) {
			List<Double> output = model.getOutputEstimation(inputArray);
			for (int i = 0; i < output.size(); i++) {
				Skill skillI = SkillEngine.getInstance().getSkill(getOwner(), skillSet.get(i), 1, getTarget());
				int cdID = skillI.getSkillTemplate().getCooldownId();

				boolean isDPskill = false;
				if (skillI.getSkillTemplate().getStartconditions() != null)
					for (Condition c : skillI.getSkillTemplate().getStartconditions().getConditions())
						if (c instanceof DpCondition)
							isDPskill = true;

				// rule out:
				if (isDPskill || getEffectController().isInAnyAbnormalState(AbnormalState.CANT_ATTACK_STATE) || !skillI.canUseSkill(CastState.CAST_START)
					|| (skillI.getSkillTemplate().getType() == SkillType.MAGICAL && getEffectController().isAbnormalSet(AbnormalState.SILENCE))
					|| (skillI.getSkillTemplate().getType() == SkillType.PHYSICAL && getEffectController().isAbnormalSet(AbnormalState.BIND))
					|| restrictedSkills.contains(skillSet.get(i)) || skillI.getSkillMethod() == SkillMethod.CHARGE || skillI.isPointSkill()
					|| getOwner().getSkillCoolDown(cdID) > System.currentTimeMillis())
					output.set(i, -1d); // -1 = minimum probability
			}

			int skillIndex = PlayerModelController.getMaxIndex(output);
			if (output.get(skillIndex) == -1) // if no CDs available: auto-attack
				return -1;

			return skillSet.get(skillIndex);
		}
		return -1;
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate) {
		super.onEndUseSkill(skillTemplate);
		previousSkill = skillTemplate.getSkillId();

		if (castTimeout != null) {
			castTimeout.cancel(true);
			castTimeout = null;
		}

		if (skillTemplate.getCooldown() == 0) // item skills: prevent spamming
			getOwner().setSkillCoolDown(skillTemplate.getCooldownId(), System.currentTimeMillis() + 60000);

		getEffectController().unsetAbnormal(AbnormalState.SANCTUARY);
	}

	private void adaptAppearance() {
		if (player == null)
			return;

		List<ItemTemplate> equipmentList = new ArrayList<>();
		if (player.getEquipment().getMainHandWeapon() != null) // weapons manually to exclude swap weapon slots
			equipmentList.add(player.getEquipment().getMainHandWeapon().getItemSkinTemplate());
		if (player.getEquipment().getOffHandWeapon() != null)
			equipmentList.add(player.getEquipment().getOffHandWeapon().getItemSkinTemplate());
		for (Item i : player.getEquipment().getEquippedItems())
			if (i.getEquipmentType() == EquipType.ARMOR)
				equipmentList.add(i.getItemSkinTemplate());

		NpcEquipmentList v = new NpcEquipmentList();
		v.items = equipmentList.toArray(new ItemTemplate[equipmentList.size()]);
		getOwner().overrideEquipmentList(v);
	}

	private void adaptStats() {
		if (player == null)
			return;

		PlayerGameStats pgs = player.getGameStats();
		List<StatSetFunction> functions = new ArrayList<>();
		functions.add(new StatSetFunction(StatEnum.EARTH_RESISTANCE, pgs.getMagicalDefenseFor(SkillElement.EARTH)));
		functions.add(new StatSetFunction(StatEnum.FIRE_RESISTANCE, pgs.getMagicalDefenseFor(SkillElement.FIRE)));
		functions.add(new StatSetFunction(StatEnum.WATER_RESISTANCE, pgs.getMagicalDefenseFor(SkillElement.WATER)));
		functions.add(new StatSetFunction(StatEnum.WIND_RESISTANCE, pgs.getMagicalDefenseFor(SkillElement.WIND)));
		functions.add(new StatSetFunction(StatEnum.ABNORMAL_RESISTANCE_ALL, pgs.getAbnormalResistance().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.ACCURACY, pgs.getAccuracy().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.AGILITY, pgs.getAgility().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.ATTACK_RANGE, 2500));
		functions.add(new StatSetFunction(StatEnum.BLOCK, pgs.getBlock().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.EVASION, pgs.getEvasion().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.HEALTH, pgs.getHealth().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.KNOWLEDGE, pgs.getKnowledge().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.MAGIC_SKILL_BOOST_RESIST, pgs.getMBResist().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.MAGICAL_RESIST, pgs.getMResist().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.MAGICAL_ACCURACY, pgs.getMAccuracy().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.MAGICAL_CRITICAL, pgs.getMCritical().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.MAIN_HAND_POWER, pgs.getMainHandPAttack().getCurrent()));
		int maxHP = pgs.getMaxHp().getCurrent();
		maxHP += maxHP * rank / 10f;
		if (onlyAttack)
			maxHP *= 10;
		functions.add(new StatSetFunction(StatEnum.MAXHP, maxHP));
		functions.add(new StatSetFunction(StatEnum.MAXMP, pgs.getMaxMp().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.OFF_HAND_ACCURACY, pgs.getOffHandPAccuracy().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.OFF_HAND_ATTACK_SPEED, pgs.getAttackSpeed().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.OFF_HAND_CRITICAL, pgs.getOffHandPCritical().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.OFF_HAND_POWER, pgs.getOffHandPAttack().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.PARRY, pgs.getParry().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.PHYSICAL_ACCURACY, pgs.getMainHandPAccuracy().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.PHYSICAL_DEFENSE, pgs.getPDef().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.PHYSICAL_CRITICAL_RESIST, pgs.getPCR().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.POWER, pgs.getPower().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.SPEED, pgs.getMovementSpeed().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.WILL, pgs.getWill().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.ATTACK_SPEED, pgs.getAttackSpeed().getCurrent()));

		switch (player.getPlayerClass()) { // npcs dont use Magical Attack
			case BARD:
			case CLERIC:
			case SORCERER:
			case SPIRIT_MASTER:
			case GUNNER:
			case RIDER:
				functions.add(new StatSetFunction(StatEnum.PHYSICAL_ATTACK, pgs.getMainHandMAttack().getCurrent()));
				functions.add(new StatSetFunction(StatEnum.PHYSICAL_CRITICAL, pgs.getMCritical().getCurrent()));
				break;
			default:
				functions.add(new StatSetFunction(StatEnum.PHYSICAL_ATTACK, pgs.getMainHandPAttack().getCurrent()));
				functions.add(new StatSetFunction(StatEnum.PHYSICAL_CRITICAL, pgs.getMainHandPCritical().getCurrent()));
		}

		getOwner().getGameStats().addEffect(null, functions);
		getOwner().getLifeStats().setCurrentHp(getOwner().getLifeStats().getMaxHp());
	}

	private void cancelTasks() {
		if (skillTask != null && !skillTask.isCancelled())
			skillTask.cancel(true);
		if (castTimeout != null && !castTimeout.isCancelled())
			castTimeout.cancel(true);
	}

	@Override
	protected void handleDied() {
		cancelTasks();
		if (model == null)
			if (onlyAttack)
				PacketSendUtility.sendMessage(player, "Remarkable... ", ChatType.BRIGHT_YELLOW_CENTER);
			else
				PacketSendUtility.sendMessage(player, "Remarkable ... I will ... remember you.", ChatType.BRIGHT_YELLOW_CENTER);
		else
			PacketSendUtility.sendMessage(player, "One day ... I will ... suppress you!", ChatType.BRIGHT_YELLOW_CENTER);
		player = null;
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelTasks();
		player = null;
		super.handleDespawned();
	}
}
