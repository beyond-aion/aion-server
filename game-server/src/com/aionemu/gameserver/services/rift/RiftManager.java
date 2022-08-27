package com.aionemu.gameserver.services.rift;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.controllers.RVController;
import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.rift.RiftLocation;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.vortex.VortexLocation;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.NpcKnownList;

/**
 * @author Source
 */
public class RiftManager {

	private static Logger log = LoggerFactory.getLogger(RiftManager.class);
	private static Map<Integer, List<Npc>> riftsPerWorld = new ConcurrentHashMap<>();
	private static Map<String, SpawnTemplate> riftGroups = new ConcurrentHashMap<>();

	public static void addRiftSpawnTemplate(SpawnGroup spawn) {
		if (spawn.hasPool()) {
			SpawnTemplate template = spawn.getSpawnTemplates().get(0);
			riftGroups.put(template.getAnchor(), template);
		} else {
			for (SpawnTemplate template : spawn.getSpawnTemplates()) {
				riftGroups.put(template.getAnchor(), template);
			}
		}
	}

	public void spawnRift(RiftLocation loc, boolean isWithGuards) {
		RiftEnum rift = RiftEnum.getRift(loc.getId());
		spawnRift(rift, null, loc, isWithGuards);
	}

	public void spawnVortex(VortexLocation loc) {
		RiftEnum rift = RiftEnum.getVortex(loc.getDefendersRace());
		spawnRift(rift, loc, null, false);
	}

	private void spawnRift(RiftEnum rift, VortexLocation vl, RiftLocation rl, boolean isWithGuards) {
		SpawnTemplate masterTemplate = riftGroups.get(rift.getMaster());
		SpawnTemplate slaveTemplate = riftGroups.get(rift.getSlave());

		if (masterTemplate == null || slaveTemplate == null) {
			return;
		}

		int spawned = 0;
		int instanceCount = World.getInstance().getWorldMap(masterTemplate.getWorldId()).getInstanceCount();

		for (int i = 1; i <= instanceCount; i++) {
			if (slaveTemplate.hasPool()) {
				slaveTemplate = slaveTemplate.changeTemplate(i);
			}
			Npc slave = spawnInstance(i, slaveTemplate, new RVController(null, rift));
			Npc master = spawnInstance(i, masterTemplate, new RVController(slave, rift, isWithGuards));

			if (rift.isVortex()) {
				vl.setVortexController((RVController) master.getController());
				spawned = vl.getSpawned().size();
				vl.getSpawned().add(master);
				vl.getSpawned().add(slave);
			} else {
				spawned = rl.getSpawned().size();
				rl.addSpawned(master);
				rl.addSpawned(slave);
			}
		}

		log.info("Rift opened: " + rift.name() + ", spawned " + spawned + " Npcs (guards=" + isWithGuards + ").");
	}

	private Npc spawnInstance(int instance, SpawnTemplate template, RVController controller) {
		NpcTemplate masterObjectTemplate = DataManager.NPC_DATA.getNpcTemplate(template.getNpcId());
		Npc npc = new Npc(controller, template, masterObjectTemplate);

		npc.setKnownlist(new NpcKnownList(npc));
		npc.setEffectController(new EffectController(npc));

		World world = World.getInstance();
		world.storeObject(npc);
		world.setPosition(npc, template.getWorldId(), instance, template.getX(), template.getY(), template.getZ(), template.getHeading());
		world.spawn(npc);
		addSpawnedRift(npc);

		return npc;
	}

	private static void addSpawnedRift(Npc rift) {
		riftsPerWorld.computeIfAbsent(rift.getWorldId(), k -> new CopyOnWriteArrayList<>()).add(rift);
	}

	public static List<Npc> getSpawnedRifts(int worldId) {
		List<Npc> rifts = riftsPerWorld.get(worldId);
		return rifts != null ? rifts : Collections.emptyList();
	}

	public static boolean removeSpawnedRift(Npc rift) {
		List<Npc> rifts = riftsPerWorld.get(rift.getWorldId());
		return rifts != null && rifts.remove(rift);
	}

	public static RiftManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder {

		private static final RiftManager INSTANCE = new RiftManager();
	}
}
