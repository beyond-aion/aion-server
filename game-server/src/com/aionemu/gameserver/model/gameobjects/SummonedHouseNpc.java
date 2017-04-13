package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;

/**
 * @author Rolandas
 */
public class SummonedHouseNpc extends SummonedObject<House> {

	String masterName;

	public SummonedHouseNpc(int objId, NpcController controller, SpawnTemplate spawnTemplate, NpcTemplate npcTemplate, House house, String masterName) {
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
	public boolean isEnemy(Creature creature) {
		return false;
	}

	@Override
	public boolean isEnemyFrom(Npc npc) {
		return false;
	}

	@Override
	public boolean isEnemyFrom(Player player) {
		return false;
	}

	@Override
	public CreatureType getType(Creature creature) {
		return CreatureType.FRIEND;
	}

}
