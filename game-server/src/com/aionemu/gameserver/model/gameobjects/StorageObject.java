package com.aionemu.gameserver.model.gameobjects;

import java.util.concurrent.atomic.AtomicReference;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HousingStorage;
import com.aionemu.gameserver.network.aion.serverpackets.SM_OBJECT_USE_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 */
public final class StorageObject extends HouseObject<HousingStorage> {

	private AtomicReference<Player> usingPlayer = new AtomicReference<Player>();

	public StorageObject(House owner, int objId, int templateId) {
		super(owner, objId, templateId);
	}

	@Override
	public void onUse(Player player) {
		if (player.getObjectId() != this.getOwnerHouse().getOwnerId()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_IS_ONLY_FOR_OWNER_VALID);
			return;
		}
		if (!usingPlayer.compareAndSet(null, player)) {
			return;
		}
		try {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_USE(getObjectTemplate().getNameId()));
			PacketSendUtility.sendPacket(player, new SM_OBJECT_USE_UPDATE(player.getObjectId(), 0, 0, this));
		}
		finally {
			usingPlayer.set(null);
		}
	}

	@Override
	public boolean canExpireNow() {
		return usingPlayer.get() == null;
	}
}
