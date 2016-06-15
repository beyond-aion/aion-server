package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.HouseDecoration;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.UseableItemObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas
 */
public class SM_HOUSE_REGISTRY extends AionServerPacket {

	int action;

	public SM_HOUSE_REGISTRY(int action) {
		this.action = action;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Player player = con.getActivePlayer();
		if (player == null)
			return;

		writeC(action);
		if (action == 1) { // Display registered objects
			if (player.getHouseRegistry() == null) {
				writeH(0);
				return;
			}
			writeH(player.getHouseRegistry().getNotSpawnedObjects().size());
			for (HouseObject<?> obj : player.getHouseRegistry().getNotSpawnedObjects()) {
				if (obj == null)
					continue;
				writeD(obj.getObjectId());
				int templateId = obj.getObjectTemplate().getTemplateId();
				writeD(templateId);
				writeD(player.getHouseObjectCooldownList().getReuseDelay(obj.getObjectId()));
				if (obj.getUseSecondsLeft() > 0)
					writeD(obj.getUseSecondsLeft());
				else
					writeD(0);

				writeDyeInfo(obj == null ? null : obj.getColor());
				writeD(0); // expiration as for armor ?

				writeC(obj.getObjectTemplate().getTypeId());
				if (obj instanceof UseableItemObject) {
					((UseableItemObject) obj).writeUsageData(getBuf());
				}
			}
		} else if (action == 2) { // Display default and registered decoration items
			writeH(player.getHouseRegistry().getDefaultParts().size() + player.getHouseRegistry().getCustomParts().size());
			for (HouseDecoration deco : player.getHouseRegistry().getDefaultParts()) {
				writeD(0);
				writeD(deco.getTemplate().getId());
			}
			for (HouseDecoration houseDecor : player.getHouseRegistry().getCustomParts()) {
				writeD(houseDecor.getObjectId());
				writeD(houseDecor.getTemplate().getId());
			}
		}
	}
}
