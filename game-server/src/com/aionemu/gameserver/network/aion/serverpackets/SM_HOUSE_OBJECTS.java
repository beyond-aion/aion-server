package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.ArrayList;

import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas
 */
public class SM_HOUSE_OBJECTS extends AionServerPacket {

	Player player;

	public SM_HOUSE_OBJECTS(Player player) {
		this.player = player;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		ArrayList<HouseObject<?>> objects = player.getHouseRegistry().getSpawnedObjects();
		writeH(objects.size());
		for (HouseObject<?> obj : objects) {
			writeD(obj.getObjectTemplate().getTemplateId());
			writeF(obj.getX());
			writeF(obj.getY());
			writeF(obj.getZ());
		}
	}
}
