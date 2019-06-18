package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.HouseRegistry;
import com.aionemu.gameserver.model.templates.housing.HousingNpc;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;

/**
 * @author Rolandas
 */
public class NpcObject extends HouseObject<HousingNpc> {

	private Npc npc = null;

	public NpcObject(HouseRegistry registry, int objId, int templateId) {
		super(registry, objId, templateId);
	}

	@Override
	public void onUse(Player player) {
		// TODO: Talk ?
	}

	@Override
	public synchronized void spawn() {
		super.spawn();
		if (npc == null) {
			HousingNpc template = getObjectTemplate();
			SpawnTemplate spawn = SpawnEngine
				.newSingleTimeSpawn(getOwnerHouse().getWorldId(), template.getNpcId(), getX(), getY(), getZ(), getHeading());
			npc = (Npc) SpawnEngine.spawnObject(spawn, getOwnerHouse().getInstanceId());
		}
	}

	@Override
	public synchronized void onDespawn() {
		super.onDespawn();
		if (npc != null) {
			npc.getController().delete();
			npc = null;
		}
	}

	@Override
	public synchronized boolean canExpireNow() {
		if (npc == null)
			return true;
		return npc.getTarget() == null;
	}

	public int getNpcObjectId() {
		return npc == null ? 0 : npc.getObjectId();
	}

}
