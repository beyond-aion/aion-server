package com.aionemu.gameserver.controllers;

import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.UseableHouseObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.housing.PlaceableHouseObject;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Rolandas
 */
public class PlaceableObjectController<T extends PlaceableHouseObject> extends VisibleObjectController<HouseObject<T>> {

	@Override
	public void onDespawn() {
		super.onDespawn();
		getOwner().onDespawn();
	}

	public void onDialogRequest(Player player) {
		if (!PositionUtil.isInTalkRange(player, getOwner())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_TOO_FAR_TO_USE());
			return;
		}
		getOwner().onDialogRequest(player);
	}

	@Override
	public void notKnow(VisibleObject object) {
		super.notKnow(object);
		if (getOwner() instanceof UseableHouseObject<?> useableHouseObject && object instanceof Player player)
			useableHouseObject.releaseOccupant(player);
	}
}
