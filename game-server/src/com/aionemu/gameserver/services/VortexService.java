package com.aionemu.gameserver.services;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.vortexspawns.VortexSpawnTemplate;
import com.aionemu.gameserver.model.vortex.VortexLocation;
import com.aionemu.gameserver.model.vortex.VortexStateType;
import com.aionemu.gameserver.services.rift.RiftInformer;
import com.aionemu.gameserver.services.rift.RiftManager;
import com.aionemu.gameserver.services.vortex.DimensionalVortex;
import com.aionemu.gameserver.services.vortex.Invasion;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Source
 */
public class VortexService {

	private final Map<Integer, DimensionalVortex<?>> activeInvasions = new ConcurrentHashMap<>();
	private Map<Integer, VortexLocation> vortex;

	public void initVortexLocations() {
		if (CustomConfig.VORTEX_ENABLED) {
			vortex = DataManager.VORTEX_DATA.getVortexLocations();

			// Spawn peace
			for (VortexLocation loc : getVortexLocations().values()) {
				spawn(loc, VortexStateType.PEACE);
			}

			// Brusthonin schedule
			CronService.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					startInvasion(1);
				}

			}, CustomConfig.VORTEX_BRUSTHONIN_SCHEDULE);

			// Theobomos schedule
			CronService.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					startInvasion(0);
				}

			}, CustomConfig.VORTEX_THEOBOMOS_SCHEDULE);
		} else {
			vortex = Collections.emptyMap();
		}
	}

	public void startInvasion(final int id) {
		final DimensionalVortex<?> invasion;

		synchronized (this) {
			if (activeInvasions.containsKey(id)) {
				return;
			}
			invasion = new Invasion(vortex.get(id));
			activeInvasions.put(id, invasion);
		}

		invasion.start();

		// Scheduled invasion end
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!invasion.isGeneratorDestroyed()) {
					stopInvasion(id);
				}
			}

		}, getDuration() * 3600 * 1000);
	}

	public void stopInvasion(int id) {
		if (!isInvasionInProgress(id)) {
			return;
		}

		DimensionalVortex<?> invasion;
		synchronized (this) {
			invasion = activeInvasions.remove(id);
		}

		if (invasion == null || invasion.isFinished()) {
			return;
		}

		invasion.stop();
	}

	public void spawn(VortexLocation loc, VortexStateType state) {
		// Spawn Dimensional Vortex
		if (state.equals(VortexStateType.INVASION)) {
			RiftManager.getInstance().spawnVortex(loc);
			RiftInformer.sendRiftsInfo(loc.getHomeWorldId());
		}

		// Spawn NPC
		List<SpawnGroup> locSpawns = DataManager.SPAWNS_DATA.getVortexSpawnsByLocId(loc.getId());
		for (SpawnGroup group : locSpawns) {
			for (SpawnTemplate st : group.getSpawnTemplates()) {
				VortexSpawnTemplate vortextemplate = (VortexSpawnTemplate) st;
				if (vortextemplate.getStateType().equals(state)) {
					loc.getSpawned().add(SpawnEngine.spawnObject(vortextemplate, 1));
				}
			}
		}
	}

	public void despawn(VortexLocation loc) {
		// Unset Vortex controller
		loc.setVortexController(null);

		// Despawn all NPC
		for (VisibleObject npc : loc.getSpawned()) {
			npc.getController().deleteIfAliveOrCancelRespawn();
		}

		loc.getSpawned().clear();
	}

	public boolean isInvasionInProgress(int id) {
		return activeInvasions.containsKey(id);
	}

	public Map<Integer, DimensionalVortex<?>> getActiveInvasions() {
		return activeInvasions;
	}

	public int getDuration() {
		return CustomConfig.VORTEX_DURATION;
	}

	public void removeDefenderPlayer(Player player) {
		for (DimensionalVortex<?> invasion : activeInvasions.values()) {
			if (invasion.getDefenders().containsKey(player.getObjectId())) {
				invasion.kickPlayer(player, false);
				return;
			}
		}
	}

	public void removeInvaderPlayer(Player player) {
		for (DimensionalVortex<?> invasion : activeInvasions.values()) {
			if (invasion.getInvaders().containsKey(player.getObjectId())) {
				invasion.kickPlayer(player, true);
				return;
			}
		}
	}

	public boolean isInvaderPlayer(Player player) {
		for (DimensionalVortex<?> invasion : activeInvasions.values()) {
			if (invasion.getInvaders().containsKey(player.getObjectId())) {
				return true;
			}
		}

		return false;
	}

	public boolean isInsideVortexZone(Player player) {
		int playerWorldId = player.getWorldId();

		if (playerWorldId == 210060000 || playerWorldId == 220050000) {
			VortexLocation loc = getLocationByWorld(playerWorldId);
			if (loc != null) {
				return loc.getPlayers().containsKey(player.getObjectId());
			}
		}

		return false;
	}

	public VortexLocation getLocationByRift(int npcId) {
		return getVortexLocation(npcId == 831141 ? 1 : 0);
	}

	public VortexLocation getLocationByWorld(int worldId) {
		if (worldId == WorldMapType.THEOBOMOS.getId()) {
			return getVortexLocation(0);
		} else if (worldId == WorldMapType.BRUSTHONIN.getId()) {
			return getVortexLocation(1);
		} else {
			return null;
		}
	}

	public VortexLocation getVortexLocation(int id) {
		return vortex.get(id);
	}

	public Map<Integer, VortexLocation> getVortexLocations() {
		return vortex;
	}

	public static VortexService getInstance() {
		return VortexServiceHolder.INSTANCE;
	}

	private static class VortexServiceHolder {

		private static final VortexService INSTANCE = new VortexService();
	}

}
