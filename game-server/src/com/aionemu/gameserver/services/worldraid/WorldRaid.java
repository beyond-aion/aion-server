package com.aionemu.gameserver.services.worldraid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.worldraid.MarkerSpot;
import com.aionemu.gameserver.model.templates.worldraid.WorldRaidLocation;
import com.aionemu.gameserver.model.templates.worldraid.WorldRaidNpc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.WorldRaidService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author Whoop, Sykra
 */
public class WorldRaid {

	private final static Logger log = LoggerFactory.getLogger(WorldRaid.class);

	private final WorldRaidLocation raidLocation;
	private final boolean useSpecialSpawnMsg;
	private final boolean sendMessages;
	private final WorldRaidDeathListener deathListener = new WorldRaidDeathListener(this);
	private final AtomicBoolean isFinished = new AtomicBoolean();
	private final AtomicBoolean isStarted = new AtomicBoolean();
	private boolean isBossKilled;
	private WorldRaidNpc randomBossTemplate;
	private Npc boss, flag, vortex;
	private List<Npc> locationMarkers = new ArrayList<>();
	private Future<?> despawnTask, preparationTask;

	public WorldRaid(WorldRaidLocation raidLocation, boolean useSpecialSpawnMsg, boolean sendMessages) {
		this.raidLocation = raidLocation;
		this.useSpecialSpawnMsg = useSpecialSpawnMsg;
		this.sendMessages = sendMessages;
	}

	public final void startWorldRaid() {
		if (isStarted.compareAndSet(false, true))
			onWorldRaidStart();
	}

	public final void stopWorldRaid() {
		if (isFinished.compareAndSet(false, true))
			onWorldRaidFinish();
	}

	private void onWorldRaidStart() {
		if (preparationTask != null)
			preparationTask.cancel(false);
		preparationTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			private int progress = 0;

			@Override
			public void run() {

				switch (progress++) {
					case 0:
						spawnAndInitMapFlag();
						broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_01());
						break;
					case 10: // 10 minutes
						spawnAndInitVortex();
						broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_02());
						break;
					case 25: // 25 minutes
						spawnAndInitMarkerSpots();
						broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_03());
						break;
					case 29: // 29 minutes
						if (!EventsConfig.WORLDRAID_ENABLE_SPAWNMSG)
							break;
						if (useSpecialSpawnMsg)
							broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_INVADE_VRITRA_SPECIAL());
						else
							broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_INVADE_VRITRA());
						break;
					case 30: // 30 minutes
						preparationTask.cancel(false);
						preparationTask = null;
						despawnNpcs(vortex);
						spawnAndInitRandomBoss();
						broadcastMessage(SM_SYSTEM_MESSAGE.STR_MSG_WORLDRAID_MESSAGE_04());
						scheduleBossDespawn();
						break;
				}
			}
		}, 0, 60000);
	}

	private void onWorldRaidFinish() {
		removeBossDeathListener();
		despawnNpcs(flag, vortex);
		despawnNpcs(locationMarkers);
		if (isBossKilled()) {
			// STR_MSG_WORLDRAID_MESSAGE_DIE_01-06
			if (randomBossTemplate.getDeathMsgId() != null)
				broadcastMessage(new SM_SYSTEM_MESSAGE(randomBossTemplate.getDeathMsgId()), true);
			cancelDespawn();
		} else {
			despawnNpcs(boss);
		}
	}

	private void scheduleBossDespawn() {
		despawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (!boss.isDead())
				WorldRaidService.getInstance().stopRaid(getLocationId());
		}, 3600 * 1000);
	}

	private void cancelDespawn() {
		if (despawnTask != null && !despawnTask.isCancelled())
			despawnTask.cancel(true);
	}

	private void despawnNpcs(Npc... npcs) {
		for (Npc npc : npcs)
			if (npc != null && !npc.isDead())
				npc.getController().delete();
	}

	private void despawnNpcs(List<Npc> npcs) {
		for (Npc npc : npcs)
			if (npc != null && !npc.isDead())
				npc.getController().delete();
	}

	private void spawnAndInitRandomBoss() {
		randomBossTemplate = Rnd.get(raidLocation.getNpcPool());
		SpawnTemplate bossTemplate = SpawnEngine.newSingleTimeSpawn(raidLocation.getMapId(), randomBossTemplate.getNpcId(), raidLocation.getX(),
			raidLocation.getY(), raidLocation.getZ(), raidLocation.getH(), 0, "world_raid_aggressive");
		Npc bossNpc = (Npc) SpawnEngine.spawnObject(bossTemplate, 1);
		if (bossNpc == null) {
			log.warn("Cannot initialize world raid boss with ID " + randomBossTemplate.getNpcId() + ". No boss was spawned.");
			return;
		}
		boss = bossNpc;
		registerBossDeathListener();
	}

	private void registerBossDeathListener() {
		if (boss != null)
			boss.getAi().addEventListener(deathListener);
	}

	private void removeBossDeathListener() {
		if (boss != null)
			boss.getAi().removeEventListener(deathListener);
	}

	private void spawnAndInitMapFlag() {
		SpawnTemplate flagTemplate = SpawnEngine.newSingleTimeSpawn(raidLocation.getMapId(), 832819, raidLocation.getX(), raidLocation.getY(),
			raidLocation.getZ(), (byte) 0);
		flag = (Npc) SpawnEngine.spawnObject(flagTemplate, 1);
	}

	private void spawnAndInitVortex() {
		SpawnTemplate vortexTemplate = SpawnEngine.newSingleTimeSpawn(raidLocation.getMapId(), 702550, raidLocation.getX(), raidLocation.getY(),
			raidLocation.getZ() + 40f, (byte) 0);
		vortex = (Npc) SpawnEngine.spawnObject(vortexTemplate, 1);
	}

	private void spawnAndInitMarkerSpots() {
		for (MarkerSpot locationMarker : raidLocation.getLocationMarkers()) {
			SpawnTemplate markerTemplate = SpawnEngine.newSingleTimeSpawn(raidLocation.getMapId(), 702548, locationMarker.getX(), locationMarker.getY(),
				locationMarker.getZ(), locationMarker.getH());
			locationMarkers.add((Npc) SpawnEngine.spawnObject(markerTemplate, 1));
		}
	}

	private void broadcastMessage(SM_SYSTEM_MESSAGE msg) {
		broadcastMessage(msg, false);
	}

	private void broadcastMessage(SM_SYSTEM_MESSAGE msg, boolean forceMsg) {
		if (msg != null && (sendMessages || forceMsg))
			World.getInstance().getWorldMap(raidLocation.getMapId()).getMainWorldMapInstance().forEachPlayer(p -> PacketSendUtility.sendPacket(p, msg));
	}

	public int getLocationId() {
		return raidLocation.getLocationId();
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

}
