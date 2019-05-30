package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas
 */
public class SM_HOUSE_OBJECTS extends AionServerPacket {

	private final List<HouseObject<?>> objects;

	public SM_HOUSE_OBJECTS(List<HouseObject<?>> objects) {
		this.objects = objects;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(objects.size());
		for (HouseObject<?> obj : objects) {
			writeD(obj.getObjectTemplate().getTemplateId());
			writeF(obj.getX());
			writeF(obj.getY());
			writeF(obj.getZ());
		}
	}
}
