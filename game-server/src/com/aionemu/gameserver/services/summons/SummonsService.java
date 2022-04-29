package com.aionemu.gameserver.services.summons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.gameserver.controllers.attack.AggroList;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

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
		PacketSendUtility.broadcastPacket(summon, new SM_EMOTION(summon, EmotionType.CHANGE_SPEED));
		PacketSendUtility.broadcastPacket(summon, new SM_SUMMON_UPDATE(summon));
		return summon;
	}

	/**
	 * Release summon
	 */
	public static final void release(Summon summon, UnsummonType unsummonType) {
		if (summon.getMode() == SummonMode.RELEASE)
			return;
		summon.getController().cancelCurrentSkill(null);
		summon.setMode(SummonMode.RELEASE);
		summon.getObserveController().notifySummonReleaseObservers();
		new ReleaseSummonTask(summon, unsummonType).scheduleOrRun();
	}

	private static class ReleaseSummonTask implements Runnable {

		private final Summon summon;
		private final UnsummonType unsummonType;
		private boolean addedMasterHate;

		public ReleaseSummonTask(Summon owner, UnsummonType unsummonType) {
			this.summon = owner;
			this.unsummonType = unsummonType;
		}

		@Override
		public void run() {
			Player master = summon.getMaster();
			VisibleObject summonObj = World.getInstance().findVisibleObject(summon.getObjectId());
			// transformed npc via SM_TRANSFORM_IN_SUMMON
			if (summonObj != null && summonObj instanceof Npc npc)
				npc.getController().delete();
			else
				summon.getController().delete();

			if (summon.equals(master.getSummon()))
				master.setSummon(null);

			switch (unsummonType) {
				case COMMAND:
				case DISTANCE:
				case UNSPECIFIED:
					// reset cooldown for summoning skill
					master.resetSkillCoolDown(summon.getSummonedBySkillId());

					if (unsummonType == UnsummonType.DISTANCE)
						PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_UNSUMMON_BY_TOO_DISTANCE());
					else
						PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_UNSUMMONED(summon.getL10n()));
					PacketSendUtility.sendPacket(master, new SM_SUMMON_PANEL_REMOVE(summon.getSummonedBySkillId()));
					PacketSendUtility.sendPacket(master, new SM_SUMMON_OWNER_REMOVE(summon.getObjectId()));
					if (!addedMasterHate)
						scheduleAddMasterHate(summon);
					break;
				case LOGOUT:
					break;
			}
		}

		public void scheduleOrRun() {
			switch (unsummonType) {
				case DISTANCE:
				case LOGOUT:
					run();
					break;
				default:
					Future<?> releaseTask = ThreadPoolManager.getInstance().schedule(this, 5000);
					if (unsummonType == UnsummonType.COMMAND) { // make it cancelable if released by master, master hate will be added delayed
						PacketSendUtility.sendPacket(summon.getMaster(), SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_UNSUMMON_FOLLOWER(summon.getL10n()));
						PacketSendUtility.sendPacket(summon.getMaster(), new SM_SUMMON_UPDATE(summon));
						summon.setReleaseTask(releaseTask);
					} else
						scheduleAddMasterHate(summon);
			}
		}

		private void scheduleAddMasterHate(Summon summon) {
			addedMasterHate = true;
			if (!summon.getMaster().isDead() && summon.getMaster().isOnline()) {
				List<AggroList> summonOnlyHaters = findSummonOnlyHaters(summon);
				if (!summonOnlyHaters.isEmpty()) // add master hate to every npc which was only attacked by the summon before
					ThreadPoolManager.getInstance().schedule(() -> {
						if (!summon.getMaster().isDead())
							summonOnlyHaters.forEach(aggroList -> aggroList.addHate(summon.getMaster(), 1));
					}, 1000);
			}
		}

		private List<AggroList> findSummonOnlyHaters(Summon owner) {
			Collection<VisibleObject> npcsKnownByMaster = owner.getMaster().getKnownList().getKnownObjects().values();
			List<AggroList> aggroLists = new ArrayList<>();
			npcsKnownByMaster.forEach(object -> {
				if (object instanceof Creature) {
					AggroList aggroList = ((Creature) object).getAggroList();
					if (aggroList.isHating(owner) && !aggroList.isHating(owner.getMaster()))
						aggroLists.add(aggroList);
				}
			});
			return aggroLists;
		}
	}

	/**
	 * Change to rest mode
	 */
	public static final void restMode(Summon summon) {
		summon.getController().cancelCurrentSkill(null);
		summon.setMode(SummonMode.REST);
		Player master = summon.getMaster();
		PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_REST_MODE(summon.getL10n()));
		PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(summon));
		summon.getLifeStats().triggerRestoreTask();
	}

	public static final void setUnkMode(Summon summon) {
		summon.setMode(SummonMode.UNK);
		Player master = summon.getMaster();
		PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(summon));
	}

	/**
	 * Change to guard mode
	 */
	public static final void guardMode(Summon summon) {
		summon.getController().cancelCurrentSkill(null);
		summon.setMode(SummonMode.GUARD);
		Player master = summon.getMaster();
		PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_GUARD_MODE(summon.getL10n()));
		PacketSendUtility.sendPacket(master, new SM_SUMMON_UPDATE(summon));
		summon.getLifeStats().triggerRestoreTask();
	}

	/**
	 * Change to attackMode
	 */
	public static final void attackMode(Summon summon) {
		summon.setMode(SummonMode.ATTACK);
		Player master = summon.getMaster();
		PacketSendUtility.sendPacket(master, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_ATTACK_MODE(summon.getL10n()));
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
