package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.stats.container.NpcLifeStats;
import com.aionemu.gameserver.model.stats.container.TrapGameStats;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.world.knownlist.NpcKnownList;

/**
 * @author ATracer
 */
public class Trap extends SummonedObject<Creature> {

	public Trap(NpcController controller, SpawnTemplate spawnTemplate, Creature creator) {
		super(controller, spawnTemplate, DataManager.NPC_DATA.getNpcTemplate(spawnTemplate.getNpcId()).getLevel(), creator);
		setMasterName("");
		setKnownlist(new NpcKnownList(this));
		setEffectController(new EffectController(this));
	}

	@Override
	protected void setupStatContainers() {
		setGameStats(new TrapGameStats(this));
		setLifeStats(new NpcLifeStats(this));
	}

	@Override
	public byte getLevel() {
		return getCreator() == null ? 1 : getCreator().getLevel();
	}

	/**
	 * @return NpcObjectType.TRAP
	 */
	@Override
	public NpcObjectType getNpcObjectType() {
		return NpcObjectType.TRAP;
	}
}
