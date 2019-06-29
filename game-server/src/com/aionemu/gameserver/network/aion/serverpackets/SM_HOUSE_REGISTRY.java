package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.HouseDecoration;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.UseableItemObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.HouseRegistry;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas
 */
public class SM_HOUSE_REGISTRY extends AionServerPacket {

	private final int action;

	public SM_HOUSE_REGISTRY(int action) {
		this.action = action;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Player player = con.getActivePlayer();
		if (player == null)
			return;
		HouseRegistry houseRegistry = player.getActiveHouse().getRegistry();

		writeC(action);
		if (action == 1) { // Display registered objects
			if (houseRegistry == null) {
				writeH(0);
				return;
			}
			writeH(houseRegistry.getNotSpawnedObjects().size());
			for (HouseObject<?> obj : houseRegistry.getNotSpawnedObjects()) {
				if (obj == null)
					continue;
				writeD(obj.getObjectId());
				int templateId = obj.getObjectTemplate().getTemplateId();
				writeD(templateId);
				writeD(player.getHouseObjectCooldowns().remainingSeconds(obj.getObjectId()));
				writeD(obj.secondsUntilExpiration());

				writeDyeInfo(obj.getColor());
				writeD(0); // expiration as for armor ?

				writeC(obj.getObjectTemplate().getTypeId());
				if (obj instanceof UseableItemObject) {
					((UseableItemObject) obj).writeUsageData(getBuf());
				}
			}
		} else if (action == 2) { // Display default and registered decoration items
			List<Integer> defaultDecorIds = houseRegistry.getOwner().getBuilding().getDefaultPartIds();
			List<HouseDecoration> unusedDecors = houseRegistry.getUnusedDecors();
			writeH(defaultDecorIds.size() + unusedDecors.size());
			for (Integer defaultPartId : defaultDecorIds) {
				writeD(0);
				writeD(defaultPartId);
			}
			for (HouseDecoration houseDecor : unusedDecors) {
				writeD(houseDecor.getObjectId());
				writeD(houseDecor.getTemplateId());
			}
		}
	}
}
