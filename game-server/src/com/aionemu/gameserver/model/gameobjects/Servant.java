package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.model.stats.container.NpcLifeStats;
import com.aionemu.gameserver.model.stats.container.ServantGameStats;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

/**
 * @author ATracer
 */
public class Servant extends SummonedObject<Creature> {

	private NpcObjectType objectType;

	/**
	 * @param objId
	 * @param controller
	 * @param spawnTemplate
	 * @param objectTemplate
	 * @param level
	 */
	public Servant(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate objectTemplate, byte level) {
		super(objId, controller, spawnTemplate, objectTemplate, level);
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

	@Override
	public String getMasterName() {
		return "";
	}

	public void setUpStats() {
		((ServantGameStats) getGameStats()).setUpStats();
	}

}
