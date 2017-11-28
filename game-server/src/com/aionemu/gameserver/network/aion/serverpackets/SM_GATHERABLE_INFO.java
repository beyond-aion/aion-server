package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_GATHERABLE_INFO extends AionServerPacket {

	private VisibleObject visibleObject;

	public SM_GATHERABLE_INFO(VisibleObject visibleObject) {
		super();
		this.visibleObject = visibleObject;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeF(visibleObject.getX());
		writeF(visibleObject.getY());
		writeF(visibleObject.getZ());
		writeD(visibleObject.getObjectId());
		writeD(visibleObject.getSpawn().getStaticId());
		writeD(visibleObject.getObjectTemplate().getTemplateId());
		if (visibleObject instanceof StaticDoor) {
			if (((StaticDoor) visibleObject).isOpen()) {
				writeH(0x09);
			} else {
				writeH(0x0A);
			}
		} else {
			writeH(1);
		}
		writeC(visibleObject.getSpawn().getHeading());
		writeD(visibleObject.getObjectTemplate().getL10nId());
		writeH(0);
		writeH(0);
		writeH(0);
		writeC(100); // unk
	}
}
