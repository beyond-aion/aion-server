package com.aionemu.gameserver.controllers;

import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.dataholders.DataManager;
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
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.HopType;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.taskmanager.tasks.PlayerMoveTaskManager;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author ATracer, RotO (Attack-speed hack protection), Sippolo
 */
public class SummonController extends CreatureController<Summon> {

	private long lastAttackMillis = 0;

	@Override
	public void notKnow(VisibleObject object) {
		super.notKnow(object);
		if (getOwner().getMaster().equals(object))
			SummonsService.release(getOwner(), UnsummonType.DISTANCE);
	}

	/**
	 * Release summon
	 */
	public void release(final UnsummonType unsummonType) {
		SummonsService.release(getOwner(), unsummonType);
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
		if (target.isDead() || target.getLifeStats().isAboutToDie() || !getOwner().isEnemy(target)) {
			PacketSendUtility.sendPacket(getMaster(), SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}

		int attackSpeed = getOwner().getGameStats().getAttackSpeed().getCurrent();
		long now = System.currentTimeMillis();
		long msSinceLastAttack = now - lastAttackMillis;
		if (msSinceLastAttack < attackSpeed && attackSpeed - msSinceLastAttack > 50) { // 50ms tolerance
			AuditLogger.log(getMaster(), "possibly used hack to speed up summon auto-attack (" + msSinceLastAttack + "ms instead of " + attackSpeed + ")");
			return;
		}
		lastAttackMillis = now;
		super.attackTarget(target, time, false);
	}

	@Override
	public void onAttack(Creature creature, Effect effect, TYPE type, int damage, boolean notifyAttack, LOG log, AttackStatus attackStatus,
		HopType hopType) {
		if (getOwner().isDead())
			return;

		// temp
		if (getOwner().getMode() == SummonMode.RELEASE)
			return;

		super.onAttack(creature, effect, type, damage, notifyAttack, log, attackStatus, hopType);
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
		SummonsService.release(getOwner(), UnsummonType.UNSPECIFIED);
	}

	public void useSkill(SkillOrder order) {
		Creature creature = getOwner();
		if (!DataManager.PET_SKILL_DATA.petHasSkill(getOwner().getObjectTemplate().getTemplateId(), order.getSkillId())) {
			// hackers!)
			return;
		}
		Skill skill = SkillEngine.getInstance().getSkill(creature, order.getSkillId(), 1, order.getTarget());
		skill.setHate(order.getHate());
		if (skill.useSkill() && order.isRelease()) {
			SummonsService.release(getOwner(), UnsummonType.UNSPECIFIED);
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
