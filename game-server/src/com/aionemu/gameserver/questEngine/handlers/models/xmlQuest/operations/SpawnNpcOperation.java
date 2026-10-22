package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.operations;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * Spawns npcs for the player, like the ambushes and props a quest step sets up. Without x/y/z they appear around the player, otherwise at
 * the given position.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpawnNpcOperation")
public class SpawnNpcOperation extends QuestOperation {

	@XmlAttribute(name = "npc_id", required = true)
	protected int npcId;
	@XmlAttribute
	protected int count = 1;
	/** seconds until the spawn despawns again, 0 keeps it until it dies */
	@XmlAttribute
	protected int lifetime;
	/** distance to the player, only used when no position is given */
	@XmlAttribute
	protected float distance = 5;
	@XmlAttribute
	protected Float x;
	@XmlAttribute
	protected Float y;
	@XmlAttribute
	protected Float z;
	@XmlAttribute
	protected Byte h;
	/** whether the spawns should attack the player right away */
	@XmlAttribute
	protected boolean aggro;
	/** seconds until the npcs appear */
	@XmlAttribute
	protected int delay;

	@Override
	public void doOperate(QuestEnv env) {
		Player player = env.getPlayer();
		int mapId = player.getWorldId();
		int instanceId = player.getInstanceId();
		if (delay > 0)
			ThreadPoolManager.getInstance().schedule(() -> spawnAll(player, mapId, instanceId), delay * 1000L);
		else
			spawnAll(player, mapId, instanceId);
	}

	private void spawnAll(Player player, int mapId, int instanceId) {
		if (player.getWorldId() != mapId || player.getInstanceId() != instanceId)
			return; // the player left before the spawns were due
		WorldPosition position = player.getPosition();
		for (int i = 0; i < count; i++) {
			float spawnX, spawnY, spawnZ;
			if (x != null && y != null && z != null) {
				spawnX = x;
				spawnY = y;
				spawnZ = z;
			} else {
				double angle = Math.toRadians(Rnd.nextFloat(360f));
				Vector3f collision = GeoService.getInstance().getClosestCollision(player, position.getX() + (float) (Math.cos(angle) * distance),
					position.getY() + (float) (Math.sin(angle) * distance), position.getZ());
				spawnX = collision.getX();
				spawnY = collision.getY();
				spawnZ = collision.getZ();
			}
			Npc npc = (Npc) SpawnEngine
				.spawnObject(SpawnEngine.newSingleTimeSpawn(mapId, npcId, spawnX, spawnY, spawnZ, h == null ? 0 : h), instanceId);
			if (npc == null)
				return; // npc id doesn't exist, the spawn engine logged it
			if (aggro)
				npc.getAggroList().addHate(player, 1000);
			if (lifetime > 0)
				ThreadPoolManager.getInstance().schedule(() -> {
					if (npc.isSpawned())
						npc.getController().delete();
				}, lifetime * 1000L);
		}
	}
}
