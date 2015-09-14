package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.PostboxObject;
import com.aionemu.gameserver.model.gameobjects.StorageObject;
import com.aionemu.gameserver.model.gameobjects.UseableItemObject;
import com.aionemu.gameserver.model.templates.housing.UseItemAction;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas
 */
public class SM_OBJECT_USE_UPDATE extends AionServerPacket {

	private int usingPlayerId;
	private int ownerPlayerId;
	private int useCount;
	private UseItemAction action = null;
	HouseObject<?> object;

	public SM_OBJECT_USE_UPDATE(int usingPlayerId, int ownerPlayerId, int useCount, HouseObject<?> object) {
		this.usingPlayerId = usingPlayerId;
		this.ownerPlayerId = ownerPlayerId;
		this.useCount = useCount;
		this.object = object;
		if (object instanceof UseableItemObject)
			this.action = ((UseableItemObject) object).getObjectTemplate().getAction();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(object.getObjectTemplate().getTypeId());
		if (object instanceof PostboxObject || object instanceof StorageObject) {
			writeD(usingPlayerId);
			writeC(1); // unk
			writeD(object.getObjectId());
		} else if (object instanceof UseableItemObject) {
			writeD(usingPlayerId);
			writeD(ownerPlayerId);
			writeD(object.getObjectId());
			writeD(useCount);
			int checkType = 0;
			if (action != null && action.getCheckType() != null)
				checkType = action.getCheckType();
			writeC(checkType);
		}
	}

}
