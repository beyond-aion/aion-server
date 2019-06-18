package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.NpcObject;
import com.aionemu.gameserver.model.gameobjects.UseableItemObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas
 */
public class SM_HOUSE_OBJECT extends AionServerPacket {

	HouseObject<?> houseObject;

	public SM_HOUSE_OBJECT(HouseObject<?> owner) {
		this.houseObject = owner;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (houseObject == null)
			return;
		Player player = con.getActivePlayer();
		if (player == null)
			return;

		House house = houseObject.getRegistry() == null ? null : houseObject.getRegistry().getOwner(); // may be null if it's a DummyHouseObject
		int templateId = houseObject.getObjectTemplate().getTemplateId();

		writeD(house == null ? 0 : house.getAddress().getId()); // if painted 0 ?
		writeD(house == null ? 0 : house.getOwnerId()); // player which owns house
		writeD(houseObject.getObjectId()); // <outlet[X]> data in house scripts
		writeD(houseObject.getObjectId()); // <outDB[X]> data in house scripts (probably DB id), where [X] is number

		writeD(templateId);
		writeF(houseObject.getX());
		writeF(houseObject.getY());
		writeF(houseObject.getZ());
		writeH(houseObject.getRotation());

		writeD(player.getHouseObjectCooldowns().remainingSeconds(houseObject.getObjectId()));
		writeD(houseObject.secondsUntilExpiration());

		writeDyeInfo(houseObject == null ? null : houseObject.getColor());
		writeD(0); // expiration as for armor ?

		byte typeId = houseObject.getObjectTemplate().getTypeId();
		writeC(typeId);

		switch (typeId) {
			case 1: // Use item
				((UseableItemObject) houseObject).writeUsageData(getBuf());
				break;
			case 7: // Npc type
				NpcObject npcObj = (NpcObject) houseObject;
				writeD(npcObj.getNpcObjectId());
				break;
		}
	}

}
