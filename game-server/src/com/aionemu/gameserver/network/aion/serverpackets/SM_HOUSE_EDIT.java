package com.aionemu.gameserver.network.aion.serverpackets;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.HouseDecoration;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.UseableItemObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas
 */
public class SM_HOUSE_EDIT extends AionServerPacket {

	private int action;
	private int storeId;
	private int itemObjectId;
	private float x, y, z;
	private int rotation;

	public SM_HOUSE_EDIT(int action) {
		this.action = action;
	}

	public SM_HOUSE_EDIT(int action, int storeId, int itemObjectId) {
		this(action);
		this.itemObjectId = itemObjectId;
		this.storeId = storeId;
	}

	public SM_HOUSE_EDIT(int action, int itemObjectId, float x, float y, float z, int rotation) {
		this.action = action;
		this.itemObjectId = itemObjectId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.rotation = rotation;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Player player = con.getActivePlayer();
		if (player == null)
			return;
		House house = player.getActiveHouse();
		HouseObject<?> obj = house.getRegistry().getObjectByObjId(itemObjectId);

		if (action == 3) { // Add item
			int templateId;
			int typeId = 0;
			if (obj == null) {
				HouseDecoration deco = house.getRegistry().getDecorByObjId(itemObjectId);
				if (deco == null) {
					LoggerFactory.getLogger(getClass()).warn("House item with object ID " + itemObjectId + " wasn't found in registry of " + house);
					return;
				}
				templateId = deco.getTemplateId();
			} else {
				templateId = obj.getObjectTemplate().getTemplateId();
				typeId = obj.getObjectTemplate().getTypeId();
			}
			writeC(action);
			writeC(storeId);
			writeD(itemObjectId);
			writeD(templateId);
			writeD(obj == null ? 0 : obj.secondsUntilExpiration());

			writeDyeInfo(obj == null ? null : obj.getColor());
			writeD(0); // expiration as for armor ?

			writeC(typeId);
			// Additional info about the usage
			if (obj instanceof UseableItemObject) {
				writeD(player.getObjectId());
				((UseableItemObject) obj).writeUsageData(getBuf());
			}
		} else if (action == 4) { // Remove from inventory
			writeC(action);
			writeC(storeId);
			writeD(itemObjectId);
		} else if (action == 5) { // Spawn or move object
			writeC(action);
			writeD(house.getAddress().getId()); // if painted 0 ?
			writeD(player.getCommonData().getPlayerObjId());
			writeD(itemObjectId);
			writeD(obj.getObjectTemplate().getTemplateId());
			writeF(x);
			writeF(y);
			writeF(z);
			writeH(rotation);
			writeD(player.getHouseObjectCooldowns().remainingSeconds(itemObjectId));
			writeD(obj.secondsUntilExpiration());
			writeDyeInfo(obj.getColor());
			writeD(0); // expiration as for armor ?

			writeC(obj.getObjectTemplate().getTypeId());

			if (obj instanceof UseableItemObject) {
				((UseableItemObject) obj).writeUsageData(getBuf());
			}
		} else if (action == 7) { // Despawn object
			writeC(action);
			writeD(itemObjectId);
		} else
			writeC(action);
	}

}
