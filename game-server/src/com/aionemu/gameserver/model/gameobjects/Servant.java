package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.model.stats.container.NpcLifeStats;
import com.aionemu.gameserver.model.stats.container.ServantGameStats;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.world.knownlist.NpcKnownList;

/**
 * @author ATracer
 */
public class Servant extends SummonedObject<Creature> {

	private NpcObjectType objectType;

	public Servant(NpcController controller, SpawnTemplate spawnTemplate, byte level, Creature creator) {
		super(controller, spawnTemplate, level, creator);
		setMasterName("");
		setKnownlist(new NpcKnownList(this));
		setEffectController(new EffectController(this));
	}

	@Override
	protected void setupStatContainers() {
		setGameStats(new ServantGameStats(this));
		setLifeStats(new NpcLifeStats(this));
	}

	@Override
	public NpcObjectType getNpcObjectType() {
		return objectType;
	}

	public void setNpcObjectType(NpcObjectType objectType) {
		this.objectType = objectType;
	}

	public void setUpStats() {
		((ServantGameStats) getGameStats()).setUpStats();
	}

}
