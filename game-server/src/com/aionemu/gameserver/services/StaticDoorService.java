package com.aionemu.gameserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Wakizashi
 */
public class StaticDoorService {

	private static final Logger log = LoggerFactory.getLogger(StaticDoorService.class);

	public static StaticDoorService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final StaticDoorService instance = new StaticDoorService();
	}

	public void openStaticDoor(Player player, int doorId) {
		StaticDoor door = getDoor(player, doorId);
		if (door == null)
			return;
		int keyId = door.getObjectTemplate().getKeyId();

		if (player.hasAccess(AdminConfig.INSTANCE_DOOR_INFO))
			PacketSendUtility.sendMessage(player, "Door ID: " + doorId + ", key ID: " + keyId);

		boolean opened = false;
		synchronized (door) {
			if (!door.isOpen() && checkStaticDoorKey(player, door, keyId)) {
				door.setOpen(true);
				opened = true;
			}
		}
		if (opened)
			player.getPosition().getWorldMapInstance().getInstanceHandler().onOpenDoor(doorId);
	}

	public void changeStaticDoorState(final Player player, int doorId, boolean open, int state) {
		StaticDoor door = getDoor(player, doorId);
		if (door == null)
			return;
		door.changeState(open, state);
		PacketSendUtility.sendMessage(player, "Door states now are: " + door.getStates());
	}

	private StaticDoor getDoor(Player player, int doorId) {
		VisibleObject object = player.getPosition().getWorldMapInstance().getObjectByStaticId(doorId);
		if (!(object instanceof StaticDoor)) {
			if (object == null)
				log.warn("Door (ID: " + doorId + ") is missing near " + player.getPosition());
			else
				log.warn("Door (ID: " + doorId + ") is not a static door but " + object);
			return null;
		}
		return (StaticDoor) object;
	}

	private boolean checkStaticDoorKey(Player player, StaticDoor door, int keyId) {
		if (player.hasAccess(AdminConfig.INSTANCE_OPEN_DOORS))
			return true;

		if (keyId == 0)
			return true;

		if (keyId == 1)
			return false;

		if (!door.isLocked()) {
			return true;
		}

		if (!player.getInventory().decreaseByItemId(keyId, 1)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_OPEN_DOOR_NEED_KEY_ITEM());
			return false;
		}

		door.setLocked(false);

		return true;
	}
}
