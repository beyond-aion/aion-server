package com.aionemu.gameserver.services.summons;

import java.util.concurrent.Future;

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
 * @author xTz
 */
public class SummonsService {

	/**
	 * create summon
	 */
	public static final Summon createSummon(Player master, int npcId, int skillId, int skillLevel, int time) {
		if (master.getSummon() != null) {
			PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_ALREADY_HAVE_A_FOLLOWER());
			return null;
		}
		Summon summon = VisibleObjectSpawner.spawnSummon(master, npcId, skillId, time);
		master.setSummon(summon);
		PacketSendUtility.sendPacket(master, new SM_SUMMON_PANEL(summon));
		PacketSendUtility.broadcastPacket(summon, new SM_EMOTION(summon, EmotionType.START_EMOTE2));
		PacketSendUtility.broadcastPacket(summon, new SM_SUMMON_UPDATE(summon));
		return summon;
	}

	/**
	 * Release summon
	 */
	public static final void release(final Summon summon, final UnsummonType unsummonType, final boolean isAttacked) {
		if (summon.getMode() == SummonMode.RELEASE)
			return;
		summon.getController().cancelCurrentSkill(null);
		summon.setMode(SummonMode.RELEASE);
		final Player master = summon.getMaster();
		switch (unsummonType) {
			case COMMAND:
				PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_UNSUMMON_FOLLOWER(summon.getNameId()));
				PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(summon));
				break;
			case DISTANCE:
				PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_UNSUMMON_BY_TOO_DISTANCE());
				PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(summon));
				break;
			case UNSPECIFIED:
			case LOGOUT:
				break;
		}
		summon.getObserveController().notifySummonReleaseObservers();
		Future<?> releaseTask = ThreadPoolManager.getInstance().schedule(new ReleaseSummonTask(summon, unsummonType, isAttacked), 5000);
		if (unsummonType == UnsummonType.COMMAND) // make it cancelable if triggered manually
			summon.setReleaseTask(releaseTask);
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

			owner.getController().delete();
			owner.setMaster(null);
			if (owner.equals(master.getSummon()))
				master.setSummon(null);

			switch (unsummonType) {
				case COMMAND:
				case DISTANCE:
				case UNSPECIFIED:
					PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_UNSUMMONED(owner.getNameId()));
					PacketSendUtility.sendPacket(master, new SM_SUMMON_PANEL_REMOVE(summonedBySkillId));
					PacketSendUtility.sendPacket(master, new SM_SUMMON_OWNER_REMOVE(owner.getObjectId()));

					// reset cooldown for summoning skill
					master.resetSkillCoolDown(summonedBySkillId);

					// TODO temp till found on retail
					if (target instanceof Creature) {
						final Creature lastAttacker = (Creature) target;
						if (!master.isDead() && !lastAttacker.isDead() && isAttacked) {
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
		summon.getController().cancelCurrentSkill(null);
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
		summon.getController().cancelCurrentSkill(null);
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
		if (summon.isDead())
			return;

		if (summon.getMaster() == null)
			return;

		if (unsummonType == UnsummonType.COMMAND && summonMode != SummonMode.RELEASE)
			summon.cancelReleaseTask();

		switch (summonMode) {
			case REST:
				summon.getController().restMode();
				break;
			case ATTACK:
				summon.getController().attackMode(targetObjId);
				break;
			case GUARD:
				summon.getController().guardMode();
				break;
			case RELEASE:
				if (unsummonType != null) {
					summon.getController().release(unsummonType);
				}
				break;
			case UNK:
				break;
		}
	}
}
