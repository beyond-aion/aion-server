package com.aionemu.gameserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.geoEngine.bounding.BoundingBox;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.StaticDoor;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Wakizashi
 */
public class StaticDoorService {

	private static final Logger log = LoggerFactory.getLogger(StaticDoorService.class);
	/**
	 * Allowed distance between the player and the bounds of the door geometry.
	 */
	private static final float MAX_DOOR_DISTANCE = 5;
	/**
	 * Allowed distance to the door position, if the door has no geometry to measure against.
	 */
	private static final float MAX_DOOR_POSITION_DISTANCE = 20;

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
		if (!isNearDoor(player, door, doorId)) {
			AuditLogger.log(player, "tried to open door " + doorId + " from " + player.getPosition());
			return;
		}

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
				log.warn("Door (ID: {}) is missing near {}", doorId, player.getPosition());
			else
				log.warn("Door (ID: {}) is not a static door but {}", doorId, object);
			return null;
		}
		return (StaticDoor) object;
	}

	private boolean isNearDoor(Player player, StaticDoor door, int doorId) {
		BoundingBox box = GeoService.getInstance().getDoorBounds(door.getWorldId(), doorId);
		if (box == null)
			return PositionUtil.isInRange(player, door, MAX_DOOR_POSITION_DISTANCE);
		Vector3f center = box.getCenter();
		return Math.abs(player.getX() - center.x) <= box.getXExtent() + MAX_DOOR_DISTANCE
			&& Math.abs(player.getY() - center.y) <= box.getYExtent() + MAX_DOOR_DISTANCE
			&& Math.abs(player.getZ() - center.z) <= box.getZExtent() + MAX_DOOR_DISTANCE;
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
