package com.aionemu.gameserver.model.templates.spawns.panesterra;

import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction;

/**
 * 
 * @author Yeats
 *
 */
public class AhserionsFlightSpawnTemplate extends SpawnTemplate {

	private int stage;
	private PanesterraFaction faction;
	
	public AhserionsFlightSpawnTemplate(SpawnGroup spawnGroup, SpawnSpotTemplate spot) {
		super(spawnGroup, spot);
	}
	
	public AhserionsFlightSpawnTemplate(SpawnGroup spawnGroup, float x, float y, float z, byte heading, int randWalk, String walkerId,
		int staticId, int fly) {
		super(spawnGroup, x, y, z, heading, randWalk, walkerId, staticId, fly);
	}
	
	public int getStage() {
		return stage;
	}
	
	public PanesterraFaction getFaction() {
		return faction;
	}
	
	public void setStage(int stage) {
		this.stage = stage;
	}
	
	public void setPanesterraTeam(PanesterraFaction faction) {
		this.faction = faction;
	}
}
