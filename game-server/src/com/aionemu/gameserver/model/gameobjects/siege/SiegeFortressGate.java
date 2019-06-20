package com.aionemu.gameserver.model.gameobjects.siege;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.geoEngine.scene.mesh.DoorGeometry;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.world.geo.GeoDoor;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Neon
 */
public class SiegeFortressGate extends SiegeNpc implements GeoDoor {

	private final DoorGeometry door;

	public SiegeFortressGate(int objId, NpcController controller, SiegeSpawnTemplate spawnTemplate, NpcTemplate objectTemplate, String meshFileName) {
		super(objId, controller, spawnTemplate, objectTemplate);
		this.door = GeoService.getInstance().getDoor(spawnTemplate.getWorldId(), meshFileName, spawnTemplate.getX(), spawnTemplate.getY(),
			spawnTemplate.getZ());
	}

	public DoorGeometry getGeometry() {
		return door;
	}
}
