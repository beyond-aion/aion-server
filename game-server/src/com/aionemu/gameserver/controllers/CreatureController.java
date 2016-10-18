package com.aionemu.gameserver.controllers;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AISubState;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.controllers.attack.AttackResult;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Homing;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.stats.container.CreatureLifeStats;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOVE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_CANCEL;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.ChargeSkill;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.Skill.SkillMethod;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.skillengine.model.SkillType;
import com.aionemu.gameserver.taskmanager.tasks.MovementNotifyTask;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.zone.ZoneInstance;
import com.aionemu.gameserver.world.zone.ZoneUpdateService;

/**
 * This class is for controlling Creatures [npc's, players etc]
 * 
 * @author -Nemesiss-, ATracer(2009-09-29), Sarynth
 * @modified by Wakizashi
 */
public abstract class CreatureController<T extends Creature> extends VisibleObjectController<Creature> {

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
		getOwner().getObserveController().notifyMoveObservers();
		notifyAIOnMove();
	}

	/**
	 * Perform tasks on Creature return at home
	 */
	public void onReturnHome() {
	}

	/**
	 * Notify everyone in knownlist about move event
	 */
	protected void notifyAIOnMove() {
		MovementNotifyTask.getInstance().add(getOwner());
	}

	/**
	 * Zone update mask management
	 * 
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
	 * Perform tasks when Creature was attacked //TODO may be pass only Skill object - but need to add properties in it
	 */
	public void onAttack(final Creature attacker, int skillId, TYPE type, int damage, boolean notifyAttack, LOG logId, AttackStatus status) {
		// avoid killing players after duel
		if (!getOwner().isEnemy(attacker) && getOwner().isPvpTarget(attacker) && !getOwner().equals(attacker))
			return;

		if (damage != 0) {
			Skill skill = getOwner().getCastingSkill();
			if (skill != null && notifyAttack) {
				if (skill.getSkillMethod() == SkillMethod.ITEM) {
					cancelCurrentSkill(attacker);
				} else {
					int cancelRate = skill.getSkillTemplate().getCancelRate();
					if (cancelRate >= 99999) {
						cancelCurrentSkill(attacker);
					} else if (cancelRate > 0 && !(getOwner() instanceof Npc && ((Npc) getOwner()).isBoss())){
						int conc = getOwner().getGameStats().getStat(StatEnum.CONCENTRATION, 0).getCurrent();
						float maxHp = getOwner().getGameStats().getMaxHp().getCurrent();
						int cancel = Math.round(((7f * (damage / maxHp) * 100f) - conc / 2f) * (cancelRate / 100f));
						if (Rnd.get(1, 100) <= cancel)
							cancelCurrentSkill(attacker);
					}
				}
			}
		}

		if (damage != 0 && notifyAttack) {
			getOwner().getObserveController().notifyAttackedObservers(attacker, skillId);
		}

		// Reduce the damage to exactly what is required to ensure death.
		// - Important that we don't include 7k worth of damage when the
		// creature only has 100 hp remaining. (For AggroList dmg count.)
		if (damage > getOwner().getLifeStats().getCurrentHp())
			damage = getOwner().getLifeStats().getCurrentHp() + 1;

		getOwner().getAggroList().addDamage(attacker, damage);
		getOwner().getLifeStats().reduceHp(type, damage, skillId, logId, attacker);

		getOwner().incrementAttackedCount();

		if (attacker instanceof Player)
			applyEffectOnCritical((Player) attacker, getOwner(), status, skillId);

		// notify all NPC's around that creature is attacking me
		getOwner().getKnownList().forEachNpc(new Consumer<Npc>() {

			@Override
			public void accept(Npc object) {
				object.getAi2().onCreatureEvent(AIEventType.CREATURE_NEEDS_SUPPORT, getOwner());
			}

		});
	}

	private void applyEffectOnCritical(Player attacker, Creature attacked, AttackStatus status, int skillId) {
		if (status == AttackStatus.CRITICAL) {
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

			if (attacked.getEffectController().isUnderShield())
				return;
			// On retail this effect apply on each crit with 10% of base chance
			// plus bonus effect penetration calculated above
			if (Rnd.get(1, 100) > 10)
				return;

			SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(id);
			// magical skills do not stun
			if (template == null || (skillId != 0 && DataManager.SKILL_DATA.getSkillTemplate(skillId).getType() == SkillType.MAGICAL))
				return;
			Effect e = new Effect(attacker, attacked, template, template.getLvl(), 0);
			e.initialize();
			e.applyEffect();

		}

	}

	/**
	 * Perform tasks when Creature was attacked
	 */
	public final void onAttack(Creature creature, int skillId, final int damage, boolean notifyAttack, AttackStatus attackStatus) {
		this.onAttack(creature, skillId, TYPE.REGULAR, damage, notifyAttack, LOG.REGULAR, attackStatus);
	}

	public final void onAttack(Creature creature, final int damage, boolean notifyAttack) {
		this.onAttack(creature, 0, TYPE.REGULAR, damage, notifyAttack, LOG.REGULAR, null);
	}

	public final void onAttack(Creature creature, final int damage, boolean notifyAttack, AttackStatus attackStatus) {
		this.onAttack(creature, 0, TYPE.REGULAR, damage, notifyAttack, LOG.REGULAR, attackStatus);
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
		if (!skipChecks && (target == null || getOwner().getLifeStats().isAlreadyDead() ||
				getOwner().getLifeStats().isAboutToDie() || !getOwner().canAttack() || !getOwner().isSpawned())) {
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
			getOwner().getObserveController().notifyGodstoneObserver(target);
		}

		final Creature creature = getOwner();
		if (time == 0)
			target.getController().onAttack(getOwner(), damage, true, firstAttackStatus);
		else
			ThreadPoolManager.getInstance().schedule(new DelayedOnAttack(target, creature, damage, firstAttackStatus), time);
	}

	/**
	 * Stops movements
	 */
	public void stopMoving() {
		Creature owner = getOwner();
		World.getInstance().updatePosition(owner, owner.getX(), owner.getY(), owner.getZ(), owner.getHeading());
		PacketSendUtility.broadcastPacket(owner, new SM_MOVE(owner));
	}

	/**
	 * Handle Dialog_Select
	 * 
	 * @param dialogId
	 * @param player
	 * @param questId
	 */
	public void onDialogSelect(int dialogId, int prevDialogId, Player player, int questId, int extendedRewardIndex) {
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
			if (visibleObject instanceof Creature)
				((Creature) visibleObject).getAggroList().notifyHate(getOwner(), value);
		});
	}

	public void abortCast() {
		Creature creature = getOwner();
		Skill skill = creature.getCastingSkill();
		if (skill == null)
			return;
		creature.setCasting(null);
		if (creature instanceof Npc) {
			removeQueuedSkill((Npc)creature);
			((Npc) creature).getGameStats().setLastSkill(null);
		}
		if (creature.getSkillNumber() > 0)
			creature.setSkillNumber(creature.getSkillNumber() - 1);
	}

	public void cancelCurrentSkill(Creature lastAttacker) {
		cancelCurrentSkill(lastAttacker, null);
	}

	/**
	 * Cancel current skill and remove cooldown
	 */
	public void cancelCurrentSkill(Creature lastAttacker, SM_SYSTEM_MESSAGE msg) {
		if (getOwner().getCastingSkill() == null) {
			return;
		}

		Creature creature = getOwner();
		Skill castingSkill = creature.getCastingSkill();
		castingSkill.cancelCast();
		creature.removeSkillCoolDown(castingSkill.getSkillTemplate().getCooldownId());
		creature.setCasting(null);
		if (creature instanceof Npc) {
			removeQueuedSkill((Npc) creature);
			((Npc) creature).getGameStats().setLastSkill(null);
		}
		PacketSendUtility.broadcastPacketAndReceive(creature, new SM_SKILL_CANCEL(creature, castingSkill.getSkillTemplate().getSkillId()));
		if (getOwner().getAi2() instanceof NpcAI2) {
			NpcAI2 npcAI = (NpcAI2) getOwner().getAi2();
			npcAI.setSubStateIfNot(AISubState.NONE);
			npcAI.onGeneralEvent(AIEventType.ATTACK_COMPLETE);
			if (creature.getSkillNumber() > 0)
				creature.setSkillNumber(creature.getSkillNumber() - 1);
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
	public void onDespawn() {
		cancelTask(TaskId.DECAY);

		Creature owner = getOwner();
		owner.getAggroList().clear();
		owner.getObserveController().clear();
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
			target.getController().onAttack(creature, finalDamage, true, attackStatus);
			target = null;
			creature = null;
		}

	}

	@Override
	public void onAfterSpawn() {
		super.onAfterSpawn();
		getOwner().revalidateZones();
	}

}
