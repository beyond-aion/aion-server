package com.aionemu.gameserver.services.monsterraid;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
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

	private final static Logger log = LoggerFactory.getLogger(MonsterRaid.class);
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

	public final void startMonsterRaid() {
		if (isStarted.compareAndSet(false, true))
			onMonsterRaidStart();
	}

	public final void stopMonsterRaid() {
		if (isFinished.compareAndSet(false, true))
			onMonsterRaidFinish();
	}

	private final void onMonsterRaidStart() {
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
			
			int progress = 0;

			@Override
			public void run() {
				switch (progress++) {
					case 0:
						spawnFlag();
						broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_01());
						break;
					case 2: // 10 minutes
						spawnVortex();
						broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_02());
						break;
					case 5: // 25 minutes
						broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_03());
						break;
					case 6: // 30 minutes
						spawnBoss();
						regDeathListener();
						broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_04());
						scheduleDespawn();
						break;
				}
			}
		}, 0, 300000);
	}

	private final void onMonsterRaidFinish() {
		rmvDeathListener();
		despawnNpcs(flag, vortex);
		if (isBossKilled()) { // TODO: Switch for different NPCs
			broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_DIE_03());
			cancelDespawn();
		} else {
			despawnNpcs(boss);
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
	
	private void despawnNpcs(Npc... npcs) {
		for (Npc npc : npcs)
			if (npc != null && !npc.isDead())
				npc.getController().delete();
	}

	private void regDeathListener() {
		getBoss().getAi().addEventListener(deathListener);
	}

	private void rmvDeathListener() {
		getBoss().getAi().removeEventListener(deathListener);
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

	private void initBoss(Npc npc) {
		if (npc == null || boss != null) {
			log.error("[MonsterRaid] Cannot initialize boss. Either no boss was spawned or attempted to initialize twice.");
			return;
		}

		boss = npc;
	}

	private void initFlag(Npc npc) {
		if (npc == null || boss != null) {
			log.error("[MonsterRaid] Cannot initialize flag. Either no flag was spawned or attempted to initialize twice.");
			return;
		}

		flag = npc;
	}

	private void initVortex(Npc npc) {
		if (npc == null || boss != null) {
			log.error("[MonsterRaid] Cannot initialize vortex. Either no vortex was spawned or attempted to initialize twice.");
			return;
		}

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
