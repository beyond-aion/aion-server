package com.aionemu.gameserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.staticdoor.StaticDoorState;
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

	public void openStaticDoor(final Player player, int doorId) {
		StaticDoor door = player.getPosition().getWorldMapInstance().getDoors().get(doorId);
		if (door == null) {
			log.warn("Door (ID: " + doorId + ") is missing near " + player.getPosition());
			return;
		}
		int keyId = door.getObjectTemplate().getKeyId();

		if (player.hasAccess(AdminConfig.INSTANCE_DOOR_INFO))
			PacketSendUtility.sendMessage(player, "Door ID: " + doorId + ", key ID: " + keyId);

		if (checkStaticDoorKey(player, door, keyId)) {
			player.getPosition().getWorldMapInstance().getInstanceHandler().onOpenDoor(doorId);
			door.setOpen(true);
		}
	}

	public void changeStaticDoorState(final Player player, int doorId, boolean open, int state) {
		StaticDoor door = player.getPosition().getWorldMapInstance().getDoors().get(doorId);
		if (door == null) {
			PacketSendUtility.sendMessage(player, "Door is not spawned!");
			return;
		}
		door.changeState(open, state);
		String currentStates = "";
		for (StaticDoorState st : StaticDoorState.values()) {
			if (st == StaticDoorState.NONE)
				continue;
			if (door.getStates().contains(st))
				currentStates += st.toString() + ", ";
		}
		if ("".equals(currentStates))
			currentStates = "NONE";
		else
			currentStates = currentStates.substring(0, currentStates.length() - 2);
		PacketSendUtility.sendMessage(player, "Door states now are: " + currentStates);
	}

	public boolean checkStaticDoorKey(Player player, StaticDoor door, int keyId) {
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
