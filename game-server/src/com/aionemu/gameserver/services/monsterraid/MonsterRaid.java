package com.aionemu.gameserver.services.monsterraid;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AbstractAI;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.monsterraid.MonsterRaidLocation;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.MonsterRaidService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author Whoop
 */
public class MonsterRaid {

	private final MonsterRaidDeathListener deathListener = new MonsterRaidDeathListener(this);
	private final AtomicBoolean isFinished = new AtomicBoolean();
	private final AtomicBoolean isStarted = new AtomicBoolean();
	private final MonsterRaidLocation mrl;
	private boolean isBossKilled;
	private Npc boss, flag, vortex;
	private Future<?> despawnTask;

	public MonsterRaid(MonsterRaidLocation mrl) {
		this.mrl = mrl;
	}

	public final void startMonsterRaid() throws RuntimeException {
		if (isStarted.compareAndSet(false, true))
			onMonsterRaidStart();
		else
			throw new RuntimeException("Attempt to start monster raid twice! ID:" + getLocationId());
	}

	public final void stopMonsterRaid() throws RuntimeException {
		if (isFinished.compareAndSet(false, true))
			onMonsterRaidFinish();
		else
			throw new RuntimeException("Attempt to stop monster raid twice! ID:" + getLocationId());
	}

	private final void onMonsterRaidStart() {
		spawnFlag();
		broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_01());
		// TODO: Better Implementation for this latency period
		ThreadPoolManager.getInstance().schedule(() -> {
			spawnVortex();
			broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_02());
			ThreadPoolManager.getInstance().schedule(() -> {
				broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_03());
				ThreadPoolManager.getInstance().schedule(() -> {
					spawnBoss();
					regDeathListener();
					broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_04());
					scheduleDespawn();
				}, 300000);
				// }, Rnd.get(50, 100) * 6000); // 5 to 10 minutes after third announce
			}, 900000);
			// }, Rnd.get(50, 1000) * 6000); // 5 to 100 minutes after second announce
		}, 600000);
		// }, Rnd.get(100, 150) * 6000); // 10 to 15 minutes after initialization
	}

	private final void onMonsterRaidFinish() {
		rmvDeathListener();
		despawnVisiualNpcs();
		if (isBossKilled()) { // TODO: Switch for different NPCs
			broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_DIE_03());
			cancelDespawn();
		} else {
			boss.getController().delete();
		}
	}

	private final void scheduleDespawn() {
		despawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (!boss.isDead())
				MonsterRaidService.getInstance().stopRaid(getLocationId());
		}, 3600 * 1000);
	}

	private final void cancelDespawn() {
		if (despawnTask != null && !despawnTask.isCancelled())
			despawnTask.cancel(true);
	}

	private void despawnVisiualNpcs() {
		if (flag != null)
			flag.getController().delete();
		if (vortex != null)
			vortex.getController().delete();
	}

	private void regDeathListener() {
		AbstractAI ai = (AbstractAI) getBoss().getAi();
		ai.addEventListener(deathListener);
	}

	private void rmvDeathListener() {
		AbstractAI ai = (AbstractAI) getBoss().getAi();
		ai.removeEventListener(deathListener);
	}

	private void spawnBoss() {
		SpawnTemplate temp = SpawnEngine.newSingleTimeSpawn(mrl.getWorldId(), Rnd.get(mrl.getNpcIds()), mrl.getX(), mrl.getY(), mrl.getZ(),
			mrl.getH());
		initBoss((Npc) SpawnEngine.spawnObject(temp, 1));
	}

	private void spawnFlag() {
		SpawnTemplate temp = SpawnEngine.newSingleTimeSpawn(mrl.getWorldId(), 832819, mrl.getX(), mrl.getY(), mrl.getZ(), mrl.getH());
		initFlag((Npc) SpawnEngine.spawnObject(temp, 1));
	}

	private void spawnVortex() {
		SpawnTemplate temp = SpawnEngine.newSingleTimeSpawn(mrl.getWorldId(), 702550, mrl.getX(), mrl.getY(), mrl.getZ() + 40f, mrl.getH());
		initVortex((Npc) SpawnEngine.spawnObject(temp, 1));
	}

	private void initBoss(Npc npc) throws RuntimeException, NullPointerException {
		if (npc == null)
			throw new NullPointerException("No boss found for Monster Raid! ID:" + getLocationId());
		if (boss != null)
			throw new RuntimeException("Tried to initialize boss twice for Monster Raid! ID:" + getLocationId());

		boss = npc;
	}

	private void initFlag(Npc npc) throws RuntimeException, NullPointerException {
		if (npc == null)
			throw new NullPointerException("No flag found for Monster Raid! ID:" + getLocationId());
		if (flag != null)
			throw new RuntimeException("Tried to initialize flag twice for Monster Raid! ID:" + getLocationId());

		flag = npc;
	}

	private void initVortex(Npc npc) throws RuntimeException, NullPointerException {
		if (npc == null)
			throw new NullPointerException("No vortex found for Monster Raid! ID:" + getLocationId());
		if (vortex != null)
			throw new RuntimeException("Tried to initialize vortex twice for Monster Raid! ID:" + getLocationId());

		vortex = npc;
	}

	private void broadcastMessage(SM_SYSTEM_MESSAGE msg) {
		if (msg != null)
			World.getInstance().getWorldMap(mrl.getWorldId()).getMainWorldMapInstance().forEachPlayer(p -> PacketSendUtility.sendPacket(p, msg));
	}

	public int getLocationId() {
		return mrl.getLocationId();
	}

	public boolean isBossKilled() {
		return isBossKilled;
	}

	public void setBossKilled(boolean bossKilled) {
		this.isBossKilled = bossKilled;
	}

	public boolean isFinished() {
		return isFinished.get();
	}

	public Npc getBoss() {
		return boss;
	}
}
