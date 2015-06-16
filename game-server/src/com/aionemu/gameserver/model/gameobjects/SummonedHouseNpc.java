package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

/**
 * @author Rolandas
 */
public class SummonedHouseNpc extends SummonedObject<House> {

	String masterName;

	public SummonedHouseNpc(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate npcTemplate, House house,
		String masterName) {
		super(objId, controller, spawnTemplate, npcTemplate, npcTemplate.getLevel());
		setCreator(house);
		this.masterName = masterName;
	}

	@Override
	public int getCreatorId() {
		return getCreator().getAddress().getId();
	}

	@Override
	public String getMasterName() {
		return masterName;
	}

	@Override
	public int getType(Creature creature) {
		return CreatureType.FRIEND.getId();
	}

	@Override
	public Creature getMaster() {
		// Not interesting, player may be offline
		return null;
	}

}
