package com.aionemu.gameserver.services.summons;

import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.controllers.SummonController;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SUMMON_OWNER_REMOVE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SUMMON_PANEL;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SUMMON_PANEL_REMOVE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SUMMON_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 *
 * @author xTz
 */
public class SummonsService {

	/**
	 * create summon
	 */
	public static final void createSummon(Player master, int npcId, int skillId, int skillLevel, int time) {
		if (master.getSummon() != null) {
			PacketSendUtility.sendPacket(master, new SM_SYSTEM_MESSAGE(1300072));
			return;
		}
		Summon summon = VisibleObjectSpawner.spawnSummon(master, npcId, skillId, skillLevel, time);
		if (summon.getAi2().getName().equals("siege_weapon")) {
			summon.getAi2().onGeneralEvent(AIEventType.SPAWNED);
		}
		master.setSummon(summon);
		PacketSendUtility.sendPacket(master, new SM_SUMMON_PANEL(summon));
		PacketSendUtility.broadcastPacket(summon, new SM_EMOTION(summon, EmotionType.START_EMOTE2));
		PacketSendUtility.broadcastPacket(summon, new SM_SUMMON_UPDATE(summon));
	}

	/**
	 * Release summon
	 */
	public static final void release(final Summon summon, final UnsummonType unsummonType, final boolean isAttacked) {
		if (summon.getMode() == SummonMode.RELEASE)
			return;
		summon.getController().cancelCurrentSkill();
		summon.setMode(SummonMode.RELEASE);
		final Player master = summon.getMaster();
		switch (unsummonType) {
			case COMMAND:
				PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_UNSUMMON_FOLLOWER(summon.getNameId()));
				PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(summon));
				break;
			case DISTANCE:
				PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_UNSUMMON_BY_TOO_DISTANCE);
				PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(summon));
				break;
			case UNSPECIFIED:
			case LOGOUT:
				break;
		}
		summon.getObserveController().notifySummonReleaseObservers();
		summon.setReleaseTask(ThreadPoolManager.getInstance().schedule(new ReleaseSummonTask(summon, unsummonType, isAttacked), 5000));
	}

	public static class ReleaseSummonTask implements Runnable {

		private Summon owner;
		private UnsummonType unsummonType;
		private Player master;
		private VisibleObject target;
		private boolean isAttacked;

		public ReleaseSummonTask(Summon owner, UnsummonType unsummonType, boolean isAttacked) {
			this.owner = owner;
			this.unsummonType = unsummonType;
			this.master = owner.getMaster();
			this.target = master.getTarget();
			this.isAttacked = isAttacked;
		}

		@Override
		public void run() {
			
			int summonedBySkillId = owner.getSummonedBySkillId();
			
			owner.getController().onDelete();
			owner.setMaster(null);
			master.setSummon(null);

			switch (unsummonType) {
				case COMMAND:
				case DISTANCE:
				case UNSPECIFIED:
					PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_UNSUMMONED(owner.getNameId()));
					PacketSendUtility.sendPacket(master, new SM_SUMMON_PANEL_REMOVE(summonedBySkillId));
					PacketSendUtility.sendPacket(master, new SM_SUMMON_OWNER_REMOVE(owner.getObjectId()));

					//reset cooldown for summoning skill
					master.resetSkillCoolDown(summonedBySkillId);
					
					// TODO temp till found on retail
					if (target instanceof Creature) {
						final Creature lastAttacker = (Creature) target;
						if (!master.getLifeStats().isAlreadyDead() && !lastAttacker.getLifeStats().isAlreadyDead() && isAttacked) {
							ThreadPoolManager.getInstance().schedule(new Runnable() {

								@Override
								public void run() {
									lastAttacker.getAggroList().addHate(master, 1);
								}

							}, 1000);
						}
					}
					break;
				case LOGOUT:
					break;
			}
		}

	}

	/**
	 * Change to rest mode
	 */
	public static final void restMode(final Summon summon) {
		summon.getController().cancelCurrentSkill();
		summon.setMode(SummonMode.REST);
		Player master = summon.getMaster();
		PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_REST_MODE(summon.getNameId()));
		PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(summon));
		summon.getLifeStats().triggerRestoreTask();
	}

	public static final void setUnkMode(final Summon summon) {
		summon.setMode(SummonMode.UNK);
		Player master = summon.getMaster();
		PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(summon));
	}

	/**
	 * Change to guard mode
	 */
	public static final void guardMode(final Summon summon) {
		summon.getController().cancelCurrentSkill();
		summon.setMode(SummonMode.GUARD);
		Player master = summon.getMaster();
		PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_GUARD_MODE(summon.getNameId()));
		PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(summon));
		summon.getLifeStats().triggerRestoreTask();
	}

	/**
	 * Change to attackMode
	 */
	public static final void attackMode(final Summon summon) {
		summon.setMode(SummonMode.ATTACK);
		Player master = summon.getMaster();
		PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_ATTACK_MODE(summon.getNameId()));
		PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(summon));
		summon.getLifeStats().cancelRestoreTask();
	}

	public static final void doMode(SummonMode summonMode, Summon summon) {
		doMode(summonMode, summon, 0, null);
	}

	public static final void doMode(SummonMode summonMode, Summon summon, UnsummonType unsummonType) {
		doMode(summonMode, summon, 0, unsummonType);
	}

	public static final void doMode(SummonMode summonMode, Summon summon, int targetObjId, UnsummonType unsummonType) {
		if (summon.getLifeStats().isAlreadyDead()) {
			return;
		}
		if (unsummonType != null && unsummonType.equals(UnsummonType.COMMAND) && !summonMode.equals(SummonMode.RELEASE)) {
			summon.cancelReleaseTask();
		}
		SummonController summonController = summon.getController();
		if (summonController == null) {
			return;
		}
		if (summon.getMaster() == null) {
			summon.getController().onDelete();
			return;
		}
		switch (summonMode) {
			case REST:
				summonController.restMode();
				break;
			case ATTACK:
				summonController.attackMode(targetObjId);
				break;
			case GUARD:
				summonController.guardMode();
				break;
			case RELEASE:
				if (unsummonType != null) {
					summonController.release(unsummonType);
				}
				break;
			case UNK:
				break;
		}
	}
}
