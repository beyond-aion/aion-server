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
		Player player = con.getActivePlayer();
		if (player == null)
			return;

		House house = houseObject.getOwnerHouse();
		int templateId = houseObject.getObjectTemplate().getTemplateId();

		writeD(house.getAddress().getId()); // if painted 0 ?
		writeD(house.getOwnerId()); // player which owns house
		writeD(houseObject.getObjectId()); // <outlet[X]> data in house scripts
		writeD(houseObject.getObjectId()); // <outDB[X]> data in house scripts (probably DB id), where [X] is number

		writeD(templateId);
		writeF(houseObject.getX());
		writeF(houseObject.getY());
		writeF(houseObject.getZ());
		writeH(houseObject.getRotation());

		writeD(player.getHouseObjectCooldownList().getReuseDelay(houseObject.getObjectId()));
		if (houseObject.getUseSecondsLeft() > 0)
			writeD(houseObject.getUseSecondsLeft());
		else
			writeD(0);

		Integer color = null;
		if (houseObject != null)
			color = houseObject.getColor();

		if (color != null && color > 0) {
			writeC(1); // Is dyed (True)
			writeC((color & 0xFF0000) >> 16);
			writeC((color & 0xFF00) >> 8);
			writeC((color & 0xFF));
		} else {
			writeC(0); // Is dyed (False)
			writeC(0);
			writeC(0);
			writeC(0);
		}
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
			default:
				break;
		}
	}

}
