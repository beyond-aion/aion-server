package com.aionemu.gameserver.controllers;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.controllers.attack.AttackResult;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.controllers.observer.TerrainZoneCollisionMaterialActor;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.animations.AttackHandAnimation;
import com.aionemu.gameserver.model.animations.AttackTypeAnimation;
import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.items.GodStone;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.GodstoneInfo;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_CANCEL;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.condition.SkillChargeCondition;
import com.aionemu.gameserver.skillengine.model.*;
import com.aionemu.gameserver.skillengine.model.Skill.SkillMethod;
import com.aionemu.gameserver.skillengine.properties.Properties.CastState;
import com.aionemu.gameserver.taskmanager.tasks.MovementNotifyTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.stats.CalculationType;
import com.aionemu.gameserver.world.geo.GeoService;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneUpdateService;

/**
 * This class is for controlling Creatures [npc's, players etc]
 * 
 * @author -Nemesiss-, ATracer(2009-09-29), Sarynth, Wakizashi
 */
public abstract class CreatureController<T extends Creature> extends VisibleObjectController<T> {

	private static final Logger log = LoggerFactory.getLogger(CreatureController.class);
	private volatile TerrainZoneCollisionMaterialActor actor;
	private final ConcurrentHashMap<Integer, Future<?>> tasks = new ConcurrentHashMap<>();

	@Override
	public void notSee(VisibleObject object, ObjectDeleteAnimation animation) {
		super.notSee(object, animation);
		if (object.equals(getOwner().getTarget()) && getOwner().getAi().getSubState() != AISubState.TARGET_LOST)
			getOwner().setTarget(null);
	}

	@Override
	public void notKnow(VisibleObject object) {
		super.notKnow(object);
		if (object instanceof Creature)
			getOwner().getAggroList().remove((Creature) object);
	}

	/**
	 * Removes owner from the visualObjects lists of all known objects who can't see him anymore.
	 */
	public void onHide() {
		getOwner().getKnownList().forEachObject(other -> other.getKnownList().updateVisibleObject(getOwner()));
	}

	/**
	 * Re-adds owner to the visualObjects lists of all known objects.
	 */
	public void onHideEnd() {
		getOwner().getKnownList().forEachObject(other -> other.getKnownList().updateVisibleObject(getOwner()));
	}

	/**
	 * Perform tasks on Creature starting to move
	 */
	public void onStartMove() {
		getOwner().getMoveController().setInMove(true);
		getOwner().getObserveController().notifyMoveObservers();
		notifyAIOnMove();
	}

	/**
	 * Perform tasks on Creature move in progress
	 */
	public void onMove() {
		getOwner().getObserveController().notifyMoveObservers();
		notifyAIOnMove();
		updateZone();
	}

	/**
	 * Perform tasks on Creature stop move
	 */
	public void onStopMove() {
		getOwner().getMoveController().setInMove(false);
		getOwner().getObserveController().notifyMoveObservers();
		notifyAIOnMove();
	}

	/**
	 * Notify everyone in knownlist about move event
	 */
	protected void notifyAIOnMove() {
		MovementNotifyTask.getInstance().add(getOwner());
	}

	/**
	 * Zone update mask management
	 */
	public final void updateZone() {
		ZoneUpdateService.getInstance().add(getOwner());
	}

	/**
	 * Will be called by ZoneManager when creature enters specific zone
	 */
	public void onEnterZone(ZoneInstance zoneInstance) {
	}

	/**
	 * Will be called by ZoneManager when player leaves specific zone
	 */
	public void onLeaveZone(ZoneInstance zoneInstance) {
	}

	/**
	 * Perform tasks on Creature death.<br>
	 * Should ONLY be called from {@link com.aionemu.gameserver.model.stats.container.CreatureLifeStats} to avoid duplicate death events.
	 */
	public void onDie(Creature lastAttacker) {
		getOwner().getMoveController().abortMove();
		getOwner().setCasting(null);
		getOwner().getEffectController().removeAllEffects();
		if (getOwner() instanceof Player && ((Player) getOwner()).getIsFlyingBeforeDeath()) {
			getOwner().unsetState(CreatureState.ACTIVE);
			getOwner().setState(CreatureState.FLOATING_CORPSE);
		} else
			getOwner().setState(CreatureState.DEAD);
		getOwner().getObserveController().notifyDeathObservers(lastAttacker);
		PacketSendUtility.broadcastPacketAndReceive(getOwner(),
			new SM_EMOTION(getOwner(), EmotionType.DIE, 0, getOwner().equals(lastAttacker) ? 0 : lastAttacker.getObjectId()));
	}

	/**
	 * Called when the creature gains or loses hate towards the attacker
	 */
	public void onAddHate(Creature attacker, boolean isNewInAggroList) {
		getOwner().getAi().onCreatureEvent(AIEventType.ATTACK, attacker);
	}

	/**
	 * Perform tasks when Creature was attacked
	 */
	public final void onAttack(Creature creature, int damage, AttackStatus attackStatus) {
		onAttack(creature, null, TYPE.REGULAR, damage, true, LOG.REGULAR, attackStatus, HopType.DAMAGE);
	}

	public final void onAttack(Creature creature, int damage, AttackStatus attackStatus, Effect criticalEffect) {
		onAttack(creature, null, TYPE.REGULAR, damage, true, LOG.REGULAR, attackStatus, HopType.DAMAGE, criticalEffect);
	}

	public final void onAttack(Effect effect, TYPE type, int damage, boolean notifyAttack, LOG logId, HopType hopType) {
		onAttack(effect.getEffector(), effect, type, damage, notifyAttack, logId, effect.getAttackStatus(), hopType, null);
	}

	public void onAttack(Creature attacker, Effect effect, TYPE type, int damage, boolean notifyAttack, LOG logId, AttackStatus status, HopType hopType) {
		onAttack(attacker, effect, type, damage, notifyAttack, logId, status, hopType, null);
	}

	/**
	 * Perform tasks when Creature was attacked
	 */
	private void onAttack(Creature attacker, Effect effect, TYPE type, int damage, boolean notifyAttack, LOG logId, AttackStatus status, HopType hopType, Effect criticalEffect) {
		if (!getOwner().isSpawned())
			return;
		if (damage != 0 && notifyAttack) {
			Skill skill = getOwner().getCastingSkill();
			if (skill != null) {
				if (skill.getSkillMethod() == SkillMethod.ITEM) {
					cancelCurrentSkill(attacker);
				} else {
					int cancelRate = skill.getSkillTemplate().getCancelRate();
					if (cancelRate >= 99999) {
						cancelCurrentSkill(attacker);
					} else if (cancelRate > 0 && !(getOwner() instanceof Npc && ((Npc) getOwner()).isBoss())) {
						int conc = getOwner().getGameStats().getStat(StatEnum.CONCENTRATION, 0).getCurrent();
						float maxHp = getOwner().getGameStats().getMaxHp().getCurrent();
						int cancel = Math.round(((7f * (damage / maxHp) * 100f) - conc / 2f) * (cancelRate / 100f));
						if (Rnd.chance() < cancel)
							cancelCurrentSkill(attacker);
					}
				}
			}
			getOwner().getObserveController().notifyAttackedObservers(attacker, effect == null ? 0 : effect.getSkillId());
		}

		getOwner().getAggroList().addDamage(attacker, damage, notifyAttack, hopType);

		// notify all NPC's around that creature is attacking me
		getOwner().getKnownList().forEachNpc(npc -> npc.getAi().onCreatureEvent(AIEventType.CREATURE_NEEDS_SUPPORT, getOwner()));
		getOwner().getLifeStats().reduceHp(type, damage, effect == null ? 0 : effect.getSkillId(), logId, attacker);
		getOwner().incrementAttackedCount();

		if (!getOwner().isDead() && attacker instanceof Player player) {
			if (criticalEffect != null) {
				criticalEffect.applyEffect();
			}
			if ((effect == null || effect.tryActivateGodstone()) && status != AttackStatus.DODGE && status != AttackStatus.RESIST)
				calculateGodStoneEffects(player);
		}
		if (effect != null && type == TYPE.DELAYDAMAGE)
			effect.broadcastHate();
	}

	private void calculateGodStoneEffects(Player attacker) {
		applyGodStoneEffect(attacker, attacker.getEquipment().getMainHandWeapon(), true);
		applyGodStoneEffect(attacker, attacker.getEquipment().getOffHandWeapon(), false);
	}

	@SuppressWarnings("lossy-conversions")
	private void applyGodStoneEffect(Player attacker, Item weapon, boolean isMainHandWeapon) {
		if (weapon == null || !weapon.hasGodStone())
			return;
		GodStone godStone = weapon.getGodStone();
		if (!godStone.tryActivate(isMainHandWeapon, getOwner()))
			return;

		GodstoneInfo godstoneInfo = godStone.getGodstoneInfo();
		ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(godStone.getItemId());
		Skill skill = SkillEngine.getInstance().getSkill(attacker, godstoneInfo.getSkillId(), godstoneInfo.getSkillLevel(), getOwner(), template);
		skill.setFirstTargetRangeCheck(false);
		if (!skill.canUseSkill(CastState.CAST_START))
			return;
		Effect effect = new Effect(skill, getOwner());
		effect.initialize();
		effect.applyEffect();
		PacketSendUtility.sendPacket(attacker, SM_SYSTEM_MESSAGE.STR_SKILL_PROC_EFFECT_OCCURRED(skill.getSkillTemplate().getL10n()));
		// Illusion Godstones
		if (godstoneInfo.getBreakProb() > 0) {
			godStone.increaseActivatedCount();
			if (godStone.getActivatedCount() > godstoneInfo.getNonBreakCount() && Rnd.get(1, 1000) <= godstoneInfo.getBreakProb()) {
				// TODO: Delay 10 Minutes, send messages etc
				// PacketSendUtility.sendPacket(owner, SM_SYSTEM_MESSAGE.STR_MSG_BREAK_PROC_REMAIN_START(equippedItem.getL10n(),
				// itemTemplate.getL10nId()));
				weapon.setGodStone(null);
				PacketSendUtility.sendPacket(attacker,
					SM_SYSTEM_MESSAGE.STR_MSG_BREAK_PROC(weapon.getL10n(), DataManager.ITEM_DATA.getItemTemplate(godStone.getItemId()).getL10n()));
				ItemPacketService.updateItemAfterInfoChange(attacker, weapon);
			}
		}
	}

	/**
	 * Perform reward operation
	 */
	public void doReward() {
	}

	public void onDialogRequest(Player player) {
	}

	public void attackTarget(Creature target, int time, boolean skipChecks) {
		boolean addAttackObservers = true;
		if (!skipChecks
			&& (target == null || getOwner().isDead() || getOwner().getLifeStats().isAboutToDie() || !getOwner().canAttack() || !getOwner().isSpawned())) {
			return;
		}

		// Calculate and apply damage
		AttackHandAnimation attackHandAnimation = AttackHandAnimation.MAIN_HAND;
		AttackTypeAnimation attackTypeAnimation = AttackTypeAnimation.MELEE;
		List<AttackResult> attackResult;

		CalculationType[] calculationTypes = new CalculationType[] { CalculationType.APPLY_POWER_SHARD_DAMAGE, CalculationType.REMOVE_POWER_SHARD };
		if (getOwner() instanceof Player p && p.getEquipment().hasDualWeaponEquipped(ItemSlot.LEFT_HAND))
			calculationTypes = ArrayUtils.add(calculationTypes, CalculationType.DUAL_WIELD);
		if (getOwner().getAttackType() == ItemAttackType.PHYSICAL)
			attackResult = AttackUtil.calculatePhysAttackResult(getOwner(), target, calculationTypes);
		else {
			attackResult = AttackUtil.calculateMagAttackResult(getOwner(), target, getOwner().getAttackType().getMagicalElement(), calculationTypes);
			attackHandAnimation = AttackHandAnimation.OFF_HAND;
		}
		if (getOwner() instanceof Npc) {
			attackHandAnimation = getOwner().getAi().modifyAttackHandAnimation(attackHandAnimation);
			attackTypeAnimation = getOwner().getAi().getAttackTypeAnimation(target);
		}

		int damage = 0;
		for (AttackResult result : attackResult) {
			if (result.getAttackStatus() == AttackStatus.RESIST || result.getAttackStatus() == AttackStatus.DODGE)
				addAttackObservers = false;
			damage += result.getDamage();
		}

		AttackStatus firstAttackStatus = AttackStatus.getBaseStatus(attackResult.get(0).getAttackStatus());
		Effect criticalEffect = null;
		if (getOwner() instanceof Player player && firstAttackStatus == AttackStatus.CRITICAL && !target.getEffectController().isUnderShield() && Rnd.chance() < 10) {
			criticalEffect = SkillEngine.getInstance().createCriticalEffect(player, target, 0);
			if (criticalEffect != null && (criticalEffect.getEffectResult() == EffectResult.DODGE || criticalEffect.getEffectResult() == EffectResult.RESIST))
				criticalEffect = null;
		}
		PacketSendUtility.broadcastPacketAndReceive(getOwner(),
			new SM_ATTACK(getOwner(), target, getOwner().getGameStats().getAttackCounter(), time, attackTypeAnimation, attackHandAnimation, attackResult, criticalEffect),
			AIEventType.CREATURE_NEEDS_HELP);

		getOwner().getGameStats().increaseAttackCounter();
		if (addAttackObservers) {
			getOwner().getObserveController().notifyAttackObservers(target, 0);
		}

		if (time == 0)
			target.getController().onAttack(getOwner(), damage, firstAttackStatus, criticalEffect);
		else
			ThreadPoolManager.getInstance().schedule(new DelayedOnAttack(target, getOwner(), damage, firstAttackStatus, criticalEffect), time);
	}

	/**
	 * Handle dialog select: getOwner() is the target or dialog sender, the given player is the one who clicked the dialog
	 */
	public void onDialogSelect(int dialogActionId, int prevDialogId, Player player, int questId, int extendedRewardIndex) {
	}

	public boolean hasTask(TaskId taskId) {
		return tasks.containsKey(taskId.ordinal());
	}

	public boolean hasScheduledTask(TaskId taskId) {
		Future<?> task = tasks.get(taskId.ordinal());
		return task != null && !task.isDone();
	}

	public Future<?> getAndRemoveTask(TaskId taskId) {
		return tasks.remove(taskId.ordinal());
	}

	public Future<?> cancelTask(TaskId taskId) {
		Future<?> task = getAndRemoveTask(taskId);
		if (task != null) {
			task.cancel(false);
		}
		return task;
	}

	public boolean cancelTaskIfPresent(TaskId taskId, Future<?> task) {
		if (tasks.remove(taskId.ordinal(), task)) {
			task.cancel(false);
			return true;
		}
		return false;
	}

	/**
	 * If task already exist - it will be canceled
	 */
	public void addTask(TaskId taskId, Future<?> task) {
		tasks.compute(taskId.ordinal(), (k, oldTask) -> {
			if (oldTask != null) {
				oldTask.cancel(false);
				if (taskId == TaskId.DESPAWN) {
					log.warn("Despawn task for " + getOwner() + " was cancelled and replaced with another one, possibly delaying the intended despawn time.");
				}
			}
			return task;
		});
	}

	/**
	 * Cancel all tasks associated with this controller
	 */
	public void cancelAllTasks() {
		for (Entry<Integer, Future<?>> e : tasks.entrySet()) {
			Future<?> task = e.getValue();
			if (task != null)
				task.cancel(false);
		}
		tasks.clear();
	}

	@Override
	public void onDelete() {
		cancelAllTasks();
		super.onDelete();
	}

	/**
	 * Die by reducing HP to 0
	 */
	public boolean die() {
		return die(null, null, getOwner());
	}

	public boolean die(Creature lastAttacker) {
		return die(null, null, lastAttacker);
	}

	public boolean die(TYPE type, LOG log, Creature lastAttacker) {
		return getOwner().getLifeStats().reduceHp(type, Integer.MAX_VALUE, 0, log, lastAttacker) == 0;
	}

	/**
	 * Use skill with default level 1
	 */
	public final boolean useSkill(int skillId) {
		return useSkill(skillId, 1);
	}

	/**
	 * @return true if successful usage
	 */
	public boolean useSkill(int skillId, int skillLevel) {
		try {
			Creature creature = getOwner();
			Skill skill = SkillEngine.getInstance().getSkill(creature, skillId, skillLevel, creature.getTarget());
			if (skill != null) {
				return skill.useSkill();
			}
		} catch (Exception ex) {
			log.error("Exception during skill use: " + skillId, ex);
		}
		return false;
	}

	public boolean useChargeSkill(Skill startSkill, long chargeTimeMillis) {
		SkillChargeCondition chargeCondition = startSkill.getSkillTemplate().getSkillChargeCondition();
		ChargeSkillEntry chargeSkill = chargeCondition == null ? null : DataManager.SKILL_CHARGE_DATA.getChargedSkillEntry(chargeCondition.getValue());
		if (chargeSkill == null || chargeTimeMillis < chargeSkill.getMinTime() * startSkill.getCastSpeedForAnimationBoostAndChargeSkills()) {
			if (getOwner() instanceof Player player)
				AuditLogger.log(player, "tried to use charge skill " + startSkill.getSkillId() + " after " + chargeTimeMillis);
			return false;
		}
		try {
			int index = 0, chargeTimeSum = 0;
			for (ChargedSkill skill : chargeSkill.getSkills()) {
				chargeTimeSum += (int) (skill.getTime() * startSkill.getCastSpeedForAnimationBoostAndChargeSkills());
				if (chargeTimeSum >= chargeTimeMillis || ++index == chargeSkill.getSkills().size() - 1)
					break;
			}
			int skillId = chargeSkill.getSkills().get(index).getId();
			ChargeSkill skill = SkillEngine.getInstance().getChargeSkill(getOwner(), skillId, startSkill.getSkillLevel(), index + 1, startSkill);
			if (skill != null)
				return skill.useSkill();
		} catch (Exception ex) {
			log.error("Could not use charge skill " + startSkill.getSkillId() + " with charge time " + chargeTimeMillis, ex);
		} finally {
			startSkill.cancelCast();
		}
		return false;
	}

	public Skill abortCast() {
		Creature creature = getOwner();
		Skill castingSkill = creature.getCastingSkill();
		if (castingSkill != null) {
			castingSkill.cancelCast();
			creature.setCasting(null);
			if (creature instanceof Npc) {
				creature.getAi().setSubStateIfNot(AISubState.NONE);
				((Npc) creature).getGameStats().setLastSkill(null);
			}
		}
		return castingSkill;
	}

	public void cancelCurrentSkill(Creature lastAttacker) {
		cancelCurrentSkill(lastAttacker, null);
	}

	/**
	 * Cancel current skill and remove cooldown
	 */
	public void cancelCurrentSkill(Creature lastAttacker, SM_SYSTEM_MESSAGE msg) {
		Skill castingSkill = abortCast();
		if (castingSkill == null)
			return;

		PacketSendUtility.broadcastPacketAndReceive(getOwner(), new SM_SKILL_CANCEL(getOwner(), castingSkill.getSkillTemplate().getSkillId()));
		if (getOwner().getAi() instanceof NpcAI npcAI) {
				npcAI.onGeneralEvent(AIEventType.ATTACK_COMPLETE);
		}
		if (lastAttacker instanceof Player) {
			PacketSendUtility.sendPacket((Player) lastAttacker, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_SKILL_CANCELED());
		}
	}

	/**
	 * Cancel use Item
	 */
	public void cancelUseItem() {
	}

	@Override
	public void onAfterSpawn() {
		super.onAfterSpawn();
		getOwner().revalidateZones();
		if (actor == null && getOwner().getMoveController() != null && GeoService.getInstance().worldHasTerrainMaterials(getOwner().getWorldId())) {
			actor = new TerrainZoneCollisionMaterialActor(getOwner());
			getOwner().getObserveController().addObserver(actor);
		}
	}

	@Override
	public void onDespawn() {
		super.onDespawn();
		if (actor != null) {
			actor.abort();
			getOwner().getObserveController().removeObserver(actor);
			actor = null;
		}
		cancelTask(TaskId.DECAY);
		getOwner().getMoveController().abortMove();
		getOwner().getAggroList().clear();
	}

	private static final class DelayedOnAttack implements Runnable {

		private Creature target;
		private Creature creature;
		private int finalDamage;
		private AttackStatus attackStatus;
		private Effect criticalEffect;

		private DelayedOnAttack(Creature target, Creature creature, int finalDamage, AttackStatus attackStatus, Effect criticalEffect) {
			this.target = target;
			this.creature = creature;
			this.finalDamage = finalDamage;
			this.attackStatus = attackStatus;
			this.criticalEffect = criticalEffect;
		}

		@Override
		public void run() {
			target.getController().onAttack(creature, finalDamage, attackStatus, criticalEffect);
			target = null;
			creature = null;
			criticalEffect = null;
		}

	}

}
