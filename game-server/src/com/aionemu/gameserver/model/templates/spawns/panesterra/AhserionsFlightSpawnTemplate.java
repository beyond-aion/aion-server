package com.aionemu.gameserver.model.templates.spawns.panesterra;

import com.aionemu.gameserver.model.templates.spawns.SpawnGroup2;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraTeamId;

/**
 * 
 * @author Yeats
 *
 */
public class AhserionsFlightSpawnTemplate extends SpawnTemplate {

	private int stage;
	private PanesterraTeamId team;
	
	public AhserionsFlightSpawnTemplate(SpawnGroup2 spawnGroup, SpawnSpotTemplate spot) {
		super(spawnGroup, spot);
	}
	
	public AhserionsFlightSpawnTemplate(SpawnGroup2 spawnGroup, float x, float y, float z, byte heading, int randWalk, String walkerId,
		int staticId, int fly) {
		super(spawnGroup, x, y, z, heading, randWalk, walkerId, staticId, fly);
	}
	
	public int getStage() {
		return stage;
	}
	
	public PanesterraTeamId getTeam() {
		return team;
	}
	
	public void setStage(int stage) {
		this.stage = stage;
	}
	
	public void setPanesterraTeam(PanesterraTeamId team) {
		this.team = team;
	}
}
