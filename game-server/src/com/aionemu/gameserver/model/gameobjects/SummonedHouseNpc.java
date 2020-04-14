package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.NpcController;
import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.CreatureType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.world.knownlist.PlayerAwareKnownList;

/**
 * @author Rolandas
 */
public class SummonedHouseNpc extends SummonedObject<House> {

	public SummonedHouseNpc(NpcController controller, SpawnTemplate spawnTemplate, House house) {
		super(controller, spawnTemplate, DataManager.NPC_DATA.getNpcTemplate(spawnTemplate.getNpcId()).getLevel(), house);
		String masterName = house.getOwnerName();
		setMasterName(masterName == null ? "" : masterName);
		setKnownlist(new PlayerAwareKnownList(this));
		setEffectController(new EffectController(this));
	}

	@Override
	public int getCreatorId() {
		return getCreator().getAddress().getId();
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
