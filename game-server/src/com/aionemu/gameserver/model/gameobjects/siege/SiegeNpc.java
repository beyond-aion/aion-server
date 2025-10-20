package com.aionemu.gameserver.model.gameobjects.siege;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;

/**
 * @author ViAl
 */
public class SiegeNpc extends Npc {

	public SiegeNpc(NpcController controller, SiegeSpawnTemplate spawnTemplate, NpcTemplate objectTemplate) {
		super(controller, spawnTemplate, objectTemplate);
	}

	public SiegeRace getSiegeRace() {
		return getSpawn().getSiegeRace();
	}

	public int getSiegeId() {
		return getSpawn().getSiegeId();
	}

	@Override
	public SiegeSpawnTemplate getSpawn() {
		return (SiegeSpawnTemplate) super.getSpawn();
	}

	/**
	 * Siege Npcs of different SiegeRaces are always hostile
	 */
	@Override
	public boolean isEnemyFrom(Creature creature) {
		if (creature instanceof SiegeNpc siegeNpc && siegeNpc.getSiegeRace() != getSiegeRace())
			return true;
		return super.isEnemyFrom(creature);
	}

}
