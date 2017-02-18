package com.aionemu.gameserver.controllers;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.controllers.attack.AggroList;
import com.aionemu.gameserver.controllers.attack.AttackResult;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Homing;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.items.GodStone;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.stats.container.CreatureLifeStats;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.GodstoneInfo;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_CANCEL;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.ChargeSkill;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.Skill.SkillMethod;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.model.SkillType;
import com.aionemu.gameserver.skillengine.properties.Properties.CastState;
import com.aionemu.gameserver.taskmanager.tasks.MovementNotifyTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneUpdateService;

/**
 * This class is for controlling Creatures [npc's, players etc]
 * 
 * @author -Nemesiss-, ATracer(2009-09-29), Sarynth
 * @modified by Wakizashi
 */
public abstract class CreatureController<T extends Creature> extends VisibleObjectController<T> {

	private static final Logger log = LoggerFactory.getLogger(CreatureController.class);
	private ConcurrentHashMap<Integer, Future<?>> tasks = new ConcurrentHashMap<>();

	@Override
	public void notSee(VisibleObject object, ObjectDeleteAnimation animation) {
		super.notSee(object, animation);
		if (object.equals(getOwner().getTarget()))
			getOwner().setTarget(null);
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
	 * 
	 * @param zoneInstance
	 */
	public void onEnterZone(ZoneInstance zoneInstance) {
	}

	/**
	 * Will be called by ZoneManager when player leaves specific zone
	 * 
	 * @param zoneInstance
	 */
	public void onLeaveZone(ZoneInstance zoneInstance) {
	}

	/**
	 * Perform tasks on Creature death
	 * 
	 * @param lastAttacker
	 */
	public void onDie(@Nonnull Creature lastAttacker) {
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
	 * 
	 * @param attacker
	 * @param isNewInAggroList
	 */
	public void onAddHate(Creature attacker, boolean isNewInAggroList) {
		getOwner().getAi().onCreatureEvent(AIEventType.ATTACK, attacker);
	}

	/**
	 * Perform tasks when Creature was attacked
	 */
	public final void onAttack(Creature creature, int damage, AttackStatus attackStatus) {
		onAttack(creature, 0, TYPE.REGULAR, damage, true, LOG.REGULAR, attackStatus, true);
	}

	public final void onAttack(Effect effect, TYPE type, int damage, boolean notifyAttack, LOG logId) {
		onAttack(effect.getEffector(), effect.getSkillId(), type, damage, notifyAttack, logId, effect.getAttackStatus(),
			!effect.isGodstoneActivated() && !effect.isPeriodic());
	}

	/**
	 * Perform tasks when Creature was attacked //TODO maybe pass only Skill object - but need to add properties in it
	 */
	public void onAttack(final Creature attacker, int skillId, TYPE type, int damage, boolean notifyAttack, LOG logId, AttackStatus status,
		boolean allowGodstoneActivation) {
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
			getOwner().getObserveController().notifyAttackedObservers(attacker, skillId);
		}

		// attacker should not earn more aggrolist dmg than the owner had hp left
		getOwner().getAggroList().addDamage(attacker, Math.min(getOwner().getLifeStats().getCurrentHp(), damage));
		getOwner().getLifeStats().reduceHp(type, damage, skillId, logId, attacker);

		getOwner().incrementAttackedCount();

		if (attacker instanceof Player) {
			Player player = (Player) attacker;
			if (status == AttackStatus.CRITICAL && Rnd.chance() < 10)
				applyEffectOnCritical(player, skillId);
			if (allowGodstoneActivation && status != AttackStatus.DODGE && status != AttackStatus.RESIST)
				calculateGodStoneEffects(player);
		}

		// notify all NPC's around that creature is attacking me
		getOwner().getKnownList().forEachNpc(npc -> npc.getAi().onCreatureEvent(AIEventType.CREATURE_NEEDS_SUPPORT, getOwner()));
	}

	private void applyEffectOnCritical(Player attacker, int skillId) {
		int id = 0;
		ItemGroup mainHandWeaponType = attacker.getEquipment().getMainHandWeaponType();
		if (mainHandWeaponType != null) {
			switch (mainHandWeaponType) {
				case POLEARM:
				case STAFF:
				case GREATSWORD:
					id = 8218; // stumble
					break;
				case BOW:
					id = 8217; // stun
			}
		}

		if (id == 0)
			return;

		if (getOwner().getEffectController().isUnderShield())
			return;

		// magical skills do not stun
		if (skillId != 0 && DataManager.SKILL_DATA.getSkillTemplate(skillId).getType() == SkillType.MAGICAL)
			return;

		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(id);
		Effect e = new Effect(attacker, getOwner(), template, template.getLvl(), 0);
		e.initialize();
		e.applyEffect();
	}

	private void calculateGodStoneEffects(Player attacker) {
		applyGodStoneEffect(attacker, attacker.getEquipment().getMainHandWeapon(), true);
		applyGodStoneEffect(attacker, attacker.getEquipment().getOffHandWeapon(), false);
	}

	private void applyGodStoneEffect(Player attacker, Item weapon, boolean isMainHandWeapon) {
		if (weapon == null || !weapon.hasGodStone())
			return;
		GodStone godStone = weapon.getGodStone();
		GodstoneInfo godStoneInfo = godStone.getGodstoneInfo();
		if (godStoneInfo == null)
			return;

		int procProbability = isMainHandWeapon ? godStoneInfo.getProbability() : godStoneInfo.getProbabilityLeft();
		procProbability -= getOwner().getGameStats().getStat(StatEnum.PROC_REDUCE_RATE, 0).getCurrent();

		if (Rnd.get(1, 1000) <= procProbability) {
			ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(godStone.getItemId());
			Skill skill = SkillEngine.getInstance().getSkill(attacker, godStoneInfo.getSkillId(), godStoneInfo.getSkillLevel(), getOwner(), template,
				false);
			skill.setFirstTargetRangeCheck(false);
			if (!skill.canUseSkill(CastState.CAST_START))
				return;
			PacketSendUtility.sendPacket(attacker, SM_SYSTEM_MESSAGE.STR_SKILL_PROC_EFFECT_OCCURRED(skill.getSkillTemplate().getNameId()));
			Effect effect = new Effect(skill, getOwner(), 0);
			effect.initialize();
			effect.applyEffect();
			// Illusion Godstones
			if (godStoneInfo.getBreakProb() > 0) {
				godStone.increaseActivatedCount();
				if (godStone.getActivatedCount() > godStoneInfo.getNonBreakCount() && Rnd.get(1, 1000) <= godStoneInfo.getBreakProb()) {
					// TODO: Delay 10 Minutes, send messages etc
					// PacketSendUtility.sendPacket(owner, SM_SYSTEM_MESSAGE.STR_MSG_BREAK_PROC_REMAIN_START(equippedItem.getNameId(),
					// itemTemplate.getNameId()));
					weapon.setGodStone(null);
					PacketSendUtility.sendPacket(attacker, SM_SYSTEM_MESSAGE.STR_MSG_BREAK_PROC(weapon.getNameId(), template.getNameId()));
					ItemPacketService.updateItemAfterInfoChange(attacker, weapon);
				}
			}
		}
	}

	/**
	 * Perform reward operation
	 */
	public void doReward() {
	}

	/**
	 * This method should be overriden in more specific controllers
	 * 
	 * @param player
	 */
	public void onDialogRequest(Player player) {
	}

	/**
	 * @param target
	 * @param time
	 */
	public void attackTarget(final Creature target, int time, boolean skipChecks) {
		boolean addAttackObservers = true;
		/**
		 * Check all prerequisites
		 */
		if (!skipChecks && (target == null || getOwner().getLifeStats().isAlreadyDead() || getOwner().getLifeStats().isAboutToDie()
			|| !getOwner().canAttack() || !getOwner().isSpawned())) {
			return;
		}

		/**
		 * Calculate and apply damage
		 */
		int attackType = 0;
		List<AttackResult> attackResult;
		if (getOwner() instanceof Homing) {
			attackResult = AttackUtil.calculateHomingAttackResult(getOwner(), target, getOwner().getAttackType().getMagicalElement());
			attackType = 1;
		} else {
			if (getOwner().getAttackType() == ItemAttackType.PHYSICAL)
				attackResult = AttackUtil.calculatePhysicalAttackResult(getOwner(), target);
			else {
				attackResult = AttackUtil.calculateMagicalAttackResult(getOwner(), target, getOwner().getAttackType().getMagicalElement());
				attackType = 1;
			}
		}

		int damage = 0;
		for (AttackResult result : attackResult) {
			if (result.getAttackStatus() == AttackStatus.RESIST || result.getAttackStatus() == AttackStatus.DODGE)
				addAttackObservers = false;
			damage += result.getDamage();
		}

		AttackStatus firstAttackStatus = AttackStatus.getBaseStatus(attackResult.get(0).getAttackStatus());

		PacketSendUtility.broadcastPacketAndReceive(getOwner(),
			new SM_ATTACK(getOwner(), target, getOwner().getGameStats().getAttackCounter(), time, attackType, attackResult),
			AIEventType.CREATURE_NEEDS_HELP);

		getOwner().getGameStats().increaseAttackCounter();
		if (addAttackObservers) {
			getOwner().getObserveController().notifyAttackObservers(target);
		}

		if (time == 0)
			target.getController().onAttack(getOwner(), damage, firstAttackStatus);
		else
			ThreadPoolManager.getInstance().schedule(new DelayedOnAttack(target, getOwner(), damage, firstAttackStatus), time);
	}

	/**
	 * Handle dialog select: getOwner() is the target or dialog sender, the given player is the one who clicked the dialog
	 */
	public void onDialogSelect(int dialogActionId, int prevDialogId, Player player, int questId, int extendedRewardIndex) {
	}

	/**
	 * @param taskId
	 * @return
	 */
	public Future<?> getTask(TaskId taskId) {
		return tasks.get(taskId.ordinal());
	}

	/**
	 * @param taskId
	 * @return
	 */
	public boolean hasTask(TaskId taskId) {
		return tasks.containsKey(taskId.ordinal());
	}

	/**
	 * @param taskId
	 * @return
	 */
	public boolean hasScheduledTask(TaskId taskId) {
		Future<?> task = tasks.get(taskId.ordinal());
		return task != null ? !task.isDone() : false;
	}

	/**
	 * @param taskId
	 */
	public Future<?> cancelTask(TaskId taskId) {
		Future<?> task = tasks.remove(taskId.ordinal());
		if (task != null) {
			task.cancel(false);
		}
		return task;
	}

	/**
	 * If task already exist - it will be canceled
	 * 
	 * @param taskId
	 * @param task
	 */
	public void addTask(TaskId taskId, Future<?> task) {
		cancelTask(taskId);
		tasks.put(taskId.ordinal(), task);
	}

	/**
	 * Cancel all tasks associated with this controller (when deleting object)
	 */
	public void cancelAllTasks() {
		for (int i : tasks.keySet()) {
			Future<?> task = tasks.get(i);
			if (task != null && i != TaskId.RESPAWN.ordinal()) {
				task.cancel(false);
			}
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
		return die(null, null);
	}

	public boolean die(TYPE type, LOG log) {
		CreatureLifeStats<?> lifeStats = getOwner().getLifeStats();
		return lifeStats.reduceHp(type, lifeStats.getCurrentHp(), 0, log, getOwner()) == 0;
	}

	/**
	 * Use skill with default level 1
	 */
	public final boolean useSkill(int skillId) {
		return useSkill(skillId, 1);
	}

	/**
	 * @param skillId
	 * @param skillLevel
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

	public boolean useChargeSkill(int skillId, int skillLevel, int time, VisibleObject firstTarget) {
		try {
			Player creature = (Player) getOwner();
			ChargeSkill skill = SkillEngine.getInstance().getChargeSkill(creature, skillId, skillLevel, firstTarget);
			if (skill != null) {
				skill.setHitTime(time);
				return skill.useSkill();
			}
		} catch (Exception ex) {
			log.error("Exception during skill use: " + skillId, ex);
		}
		return false;
	}

	/**
	 * Notify hate value to all visible creatures
	 * 
	 * @param value
	 */
	public void broadcastHate(int value) {
		getOwner().getKnownList().forEachObject(visibleObject -> {
			if (visibleObject instanceof Creature) {
				AggroList al = ((Creature) visibleObject).getAggroList();
				if (al.isHating(getOwner()))
					al.addHate(getOwner(), value);
			}
		});
	}

	public Skill abortCast() {
		Creature creature = getOwner();
		Skill castingSkill = creature.getCastingSkill();
		if (castingSkill != null) {
			castingSkill.cancelCast();
			creature.setCasting(null);
			if (creature instanceof Npc) {
				((NpcAI) creature.getAi()).setSubStateIfNot(AISubState.NONE);
				removeQueuedSkill((Npc) creature);
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
		if (getOwner().getAi() instanceof NpcAI) {
			NpcAI npcAI = (NpcAI) getOwner().getAi();
			npcAI.onGeneralEvent(AIEventType.ATTACK_COMPLETE);
		}
		if (lastAttacker instanceof Player) {
			PacketSendUtility.sendPacket((Player) lastAttacker, SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_SKILL_CANCELED());
		}
	}

	private void removeQueuedSkill(Npc npc) {
		NpcSkillEntry lastSkill = npc.getGameStats().getLastSkill();
		if (lastSkill != null && lastSkill.isQueued()) {
			npc.getQueuedSkills().poll();
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
	}

	@Override
	public void onDespawn() {
		cancelTask(TaskId.DECAY);
		getOwner().getAggroList().clear();
	}

	private static final class DelayedOnAttack implements Runnable {

		private Creature target;
		private Creature creature;
		private int finalDamage;
		private AttackStatus attackStatus;

		public DelayedOnAttack(Creature target, Creature creature, int finalDamage, AttackStatus attackStatus) {
			this.target = target;
			this.creature = creature;
			this.finalDamage = finalDamage;
			this.attackStatus = attackStatus;
		}

		@Override
		public void run() {
			target.getController().onAttack(creature, finalDamage, attackStatus);
			target = null;
			creature = null;
		}

	}

}
