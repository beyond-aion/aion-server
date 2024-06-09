package ai.instance.custom.eternalChallenge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.custom.instance.CustomInstanceService;
import com.aionemu.gameserver.custom.instance.RoahCustomInstanceHandler;
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
import com.aionemu.gameserver.model.gameobjects.state.CreatureSeeState;
import com.aionemu.gameserver.model.stats.calc.functions.StatSetFunction;
import com.aionemu.gameserver.model.stats.container.PlayerGameStats;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.EquipType;
import com.aionemu.gameserver.model.templates.item.enums.ItemSubType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
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
import com.aionemu.gameserver.utils.stats.CalculationType;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.GeneralNpcAI;

/**
 * @author Jo
 */
@AIName("custom_instance_boss")
public class CustomInstanceBossAI extends GeneralNpcAI {

	private static final Logger log = LoggerFactory.getLogger("CUSTOM_INSTANCE_LOG");
	private PlayerModel model;
	private Future<?> skillTask, castTimeout;
	private int previousSkill, rank;

	private List<Integer> skillSet;
	private boolean onlyAttack;

	public CustomInstanceBossAI(Npc owner) {
		super(owner);
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		return damage * 0.42f; // pseudo PvP reduce
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		return damage * 0.58f; // pseudo PvP reduce
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		previousSkill = -1;

		WorldMapInstance wmi = getPosition().getWorldMapInstance();
		if (!(wmi.getInstanceHandler() instanceof RoahCustomInstanceHandler))
			return;

		int playerId = wmi.getRegisteredObjects().iterator().next();
		rank = CustomInstanceService.getInstance().loadOrCreateRank(playerId).getRank();

		Player p = World.getInstance().getPlayer(playerId);
		if (p == null) {
			log.error("[CI_ROAH] No player object found for player id: " + playerId
				+ ". Either the player is offline or the central artifact was destroyed by something else.", new Exception());
			return;
		}

		if (p.getPlayerClass() == PlayerClass.RIDER) {
			onlyAttack = true;
		} else {
			onlyAttack = false;
			adaptAppearance(p);
			adaptStats(p);
			getOwner().setSeeState(CreatureSeeState.SEARCH2);
			// model player behavior
			skillSet = ((RoahCustomInstanceHandler) wmi.getInstanceHandler()).getSkillSet();
			model = ((RoahCustomInstanceHandler) wmi.getInstanceHandler()).getPlayerModel();
		}

	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();

		// prevent reset-abusing
		if (getOwner().getSkillCoolDowns() != null)
			getOwner().getSkillCoolDowns().clear();
		getLifeStats().setCurrentHpPercent(100);
	}

	@Override
	public void handleCreatureDetected(Creature creature) {
		super.handleCreatureDetected(creature);
		WorldMapInstance wmi = getPosition().getWorldMapInstance();
		if (!(wmi.getInstanceHandler() instanceof RoahCustomInstanceHandler))
			return;

		if (PositionUtil.getDistance(getPosition().getX(), getPosition().getY(), getPosition().getZ(), creature.getX(), creature.getY(),
			creature.getZ()) <= 45 && getPosition().getWorldMapInstance().isRegistered(creature.getObjectId())) {
			getAggroList().addHate(creature, 100); // early aggro

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
					castTimeout = ThreadPoolManager.getInstance().schedule(() -> castTimeout = null, 1000);
				}, castDuration); // after cast / instant skill: 300ms * attack speed
			}
		}
	}

	private int getPlayerSkill() {
		if (getTarget() == null || !(getTarget() instanceof Creature))
			return -1;

		// remove shock
		if ((getEffectController().isInAnyAbnormalState(AbnormalState.ANY_STUN) || getEffectController().isAbnormalSet(AbnormalState.OPENAERIAL))
			&& getOwner().getSkillCoolDown(1968) <= System.currentTimeMillis())
			return 283;

		if (getEffectController().isInAnyAbnormalState(AbnormalState.CANT_ATTACK_STATE))
			return -1;

		// compute skill selection:
		if (model != null) {
			// assess game state:
			double[] inputArray = new PlayerModelEntry(getOwner(), -1, (Creature) getTarget()).toStateInputArray(skillSet, previousSkill);

			List<Double> output = model.getOutputEstimation(inputArray);
			for (int i = 0; i < output.size(); i++) {
				Skill skillI = SkillEngine.getInstance().getSkill(getOwner(), skillSet.get(i), 1, getTarget());
				if (skillI == null) {
					log.warn("Detected a skill input with not existent template [skillId=" + skillSet.get(i) + "].");
					output.set(i, -1d);
					continue;
				}

				int cdID = -1; // item skills that have no cdID
				boolean isDPskill = false;
				if (skillI.getSkillTemplate() != null) {
					cdID = skillI.getSkillTemplate().getCooldownId();
					if (skillI.getSkillTemplate().getStartconditions() != null) {
						for (Condition c : skillI.getSkillTemplate().getStartconditions().getConditions()) {
							if (c instanceof DpCondition) {
								isDPskill = true;
								break;
							}
						}
					}
				}

				// rule out:
				if (isDPskill || !skillI.canUseSkill(CastState.CAST_START)
					|| (skillI.getSkillTemplate().getType() == SkillType.MAGICAL && getEffectController().isAbnormalSet(AbnormalState.SILENCE))
					|| (skillI.getSkillTemplate().getType() == SkillType.PHYSICAL && getEffectController().isAbnormalSet(AbnormalState.BIND))
					|| skillI.getSkillMethod() == SkillMethod.CHARGE || skillI.isPointSkill()
					|| (cdID != -1 && getOwner().getSkillCoolDown(cdID) > System.currentTimeMillis()))
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
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		super.onEndUseSkill(skillTemplate, skillLevel);
		previousSkill = skillTemplate.getSkillId();

		if (castTimeout != null) {
			castTimeout.cancel(true);
			castTimeout = null;
		}

		if (skillTemplate.getCooldown() == 0) // item skills: prevent spamming
			getOwner().setSkillCoolDown(skillTemplate.getCooldownId(), System.currentTimeMillis() + 60000);

		getEffectController().unsetAbnormal(AbnormalState.SANCTUARY);
	}

	private void adaptAppearance(Player player) {
		List<ItemTemplate> equipmentList = new ArrayList<>();
		if (player.getEquipment().getMainHandWeapon() != null) // weapons manually to exclude swap weapon slots
			equipmentList.add(player.getEquipment().getMainHandWeapon().getItemSkinTemplate());
		if (player.getEquipment().getOffHandWeapon() != null)
			equipmentList.add(player.getEquipment().getOffHandWeapon().getItemSkinTemplate());
		for (Item i : player.getEquipment().getEquippedItems())
			if (i.getEquipmentType() == EquipType.ARMOR && i.getItemTemplate().getItemSubType() != ItemSubType.SHIELD)
				equipmentList.add(i.getItemSkinTemplate());

		NpcEquipmentList v = new NpcEquipmentList();
		v.items = equipmentList.toArray(new ItemTemplate[0]);
		getOwner().overrideEquipmentList(v);
	}

	private void adaptStats(Player player) {
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
		functions.add(new StatSetFunction(StatEnum.MAIN_HAND_POWER, pgs.getMainHandPAttack(CalculationType.DISPLAY).getCurrent()));
		int maxHP = pgs.getMaxHp().getCurrent();
		maxHP += (int) (maxHP * rank / 10f);
		if (onlyAttack)
			maxHP *= 10;
		functions.add(new StatSetFunction(StatEnum.MAXHP, maxHP));
		functions.add(new StatSetFunction(StatEnum.MAXMP, pgs.getMaxMp().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.OFF_HAND_ACCURACY, pgs.getOffHandPAccuracy().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.OFF_HAND_ATTACK_SPEED, pgs.getAttackSpeed().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.OFF_HAND_CRITICAL, pgs.getOffHandPCritical().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.OFF_HAND_POWER, pgs.getOffHandPAttack(CalculationType.DISPLAY).getCurrent()));
		functions.add(new StatSetFunction(StatEnum.PARRY, pgs.getParry().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.PHYSICAL_ACCURACY, pgs.getMainHandPAccuracy().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.PHYSICAL_DEFENSE, pgs.getPDef().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.PHYSICAL_CRITICAL_RESIST, pgs.getPCR().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.POWER, pgs.getPower().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.SPEED, pgs.getMovementSpeed().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.WILL, pgs.getWill().getCurrent()));
		functions.add(new StatSetFunction(StatEnum.ATTACK_SPEED, pgs.getAttackSpeed().getCurrent()));
		// Work-around for not considered dual wield stats for NPCs
		int pAtk = pgs.getMainHandPAttack().getCurrent();
		if (player.getEquipment().getOffHandWeapon() != null)
			pAtk += pgs.getOffHandPAttack(CalculationType.DISPLAY).getCurrent() / 2;
		functions.add(new StatSetFunction(StatEnum.PHYSICAL_ATTACK, pAtk));

		if (player.getPlayerClass().isPhysicalClass()) {
			functions.add(new StatSetFunction(StatEnum.PHYSICAL_CRITICAL, pgs.getMainHandPCritical().getCurrent()));
		} else {
			functions.add(new StatSetFunction(StatEnum.MAGICAL_CRITICAL, pgs.getMCritical().getCurrent()));
			functions.add(new StatSetFunction(StatEnum.MAGICAL_ATTACK, pgs.getMainHandMAttack().getCurrent()));
			functions.add(new StatSetFunction(StatEnum.BOOST_MAGICAL_SKILL, pgs.getMBoost().getCurrent()));
		}

		getOwner().getGameStats().addEffect(null, functions);
		getLifeStats().setCurrentHp(getLifeStats().getMaxHp());
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
				PacketSendUtility.broadcastToMap(getOwner(), new SM_MESSAGE(getOwner(), "Remarkable... ", ChatType.BRIGHT_YELLOW_CENTER));
			else
				PacketSendUtility.broadcastToMap(getOwner(),
					new SM_MESSAGE(getOwner(), "Remarkable ... I will ... remember you.", ChatType.BRIGHT_YELLOW_CENTER));
		else
			PacketSendUtility.broadcastToMap(getOwner(), new SM_MESSAGE(getOwner(), "One day ... I will ... suppress you!", ChatType.BRIGHT_YELLOW_CENTER));
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		cancelTasks();
		super.handleDespawned();
	}
}
