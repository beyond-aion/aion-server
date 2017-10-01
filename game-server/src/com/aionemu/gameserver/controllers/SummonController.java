package com.aionemu.gameserver.controllers;

import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.summons.SkillOrder;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SUMMON_UPDATE;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.taskmanager.tasks.PlayerMoveTaskManager;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 * @author RotO (Attack-speed hack protection) modified by Sippolo
 */
public class SummonController extends CreatureController<Summon> {

	private long lastAttackMilis = 0;
	private boolean isAttacked = false;

	@Override
	public void notSee(VisibleObject object, ObjectDeleteAnimation animation) {
		super.notSee(object, animation);
		if (getOwner().getMaster() == null)
			return;

		if (getOwner().getMaster().equals(object)) {
			SummonsService.release(getOwner(), UnsummonType.DISTANCE, isAttacked);
		}
	}

	/**
	 * Release summon
	 */
	public void release(final UnsummonType unsummonType) {
		SummonsService.release(getOwner(), unsummonType, isAttacked);
	}

	/**
	 * Change to rest mode
	 */
	public void restMode() {
		SummonsService.restMode(getOwner());
	}

	public void setUnkMode() {
		SummonsService.setUnkMode(getOwner());
	}

	/**
	 * Change to guard mode
	 */
	public void guardMode() {
		SummonsService.guardMode(getOwner());
	}

	/**
	 * Change to attackMode
	 */
	public void attackMode(int targetObjId) {
		VisibleObject obj = getOwner().getKnownList().getObject(targetObjId);
		if (obj instanceof Creature) {
			SummonsService.attackMode(getOwner());
		}
	}

	@Override
	public void attackTarget(Creature target, int time, boolean skipChecks) {
		Player master = getOwner().getMaster();

		if (!RestrictionsManager.canAttack(master, target))
			return;

		int attackSpeed = getOwner().getGameStats().getAttackSpeed().getCurrent();
		long milis = System.currentTimeMillis();
		if (milis - lastAttackMilis < attackSpeed) {
			/**
			 * Hack!
			 */
			return;
		}
		lastAttackMilis = milis;
		super.attackTarget(target, time, false);
	}

	@Override
	public void onAttack(Creature creature, int skillId, TYPE type, int damage, boolean notifyAttack, LOG log, AttackStatus attackStatus,
		boolean allowGodstoneActivation) {
		if (getOwner().isDead())
			return;

		// temp
		if (getOwner().getMode() == SummonMode.RELEASE)
			return;

		super.onAttack(creature, skillId, type, damage, notifyAttack, log, attackStatus, allowGodstoneActivation);
		PacketSendUtility.sendPacket(getOwner().getMaster(), new SM_SUMMON_UPDATE(getOwner()));
	}

	@Override
	public void onDespawn() {
		if (getOwner().getMode() == SummonMode.RELEASE)
			getOwner().getEffectController().removeAllEffects();
		super.onDespawn();
	}

	@Override
	public void onDie(Creature lastAttacker) {
		super.onDie(lastAttacker);
		Summon owner = getOwner();
		Player master = getOwner().getMaster();
		SummonsService.release(owner, UnsummonType.UNSPECIFIED, isAttacked);

		if (!master.equals(lastAttacker) && !owner.equals(lastAttacker) && !master.isDead() && !lastAttacker.isDead()) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					lastAttacker.getAggroList().addHate(master, 1);
				}
			}, 1000);
		}
	}

	public void useSkill(SkillOrder order) {
		Creature creature = getOwner();
		if (!DataManager.PET_SKILL_DATA.petHasSkill(getOwner().getObjectTemplate().getTemplateId(), order.getSkillId())) {
			// hackers!)
			return;
		}
		Skill skill = SkillEngine.getInstance().getSkill(creature, order.getSkillId(), 1, order.getTarget());
		if (skill.useSkill() && order.isRelease()) {
			SummonsService.release(getOwner(), UnsummonType.UNSPECIFIED, isAttacked);
		}
	}

	@Override
	public void onStartMove() {
		super.onStartMove();
		PlayerMoveTaskManager.getInstance().addPlayer(getOwner());
		updateZone();
	}

	@Override
	public void onStopMove() {
		super.onStopMove();
		PlayerMoveTaskManager.getInstance().removePlayer(getOwner());
	}

	protected Player getMaster() {
		return getOwner().getMaster();
	}
}
