package com.aionemu.gameserver.controllers;

import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HouseAddress;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_RENDER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Rolandas
 */
public class HouseController extends VisibleObjectController<House> {

	@Override
	public void see(VisibleObject object) {
		if (object instanceof Player)
			spawnObjects();
	}

	public void spawnObjects() {
		if (getOwner().getRegistry() != null) {
			for (HouseObject<?> obj : getOwner().getRegistry().getSpawnedObjects()) {
				obj.spawn();
			}
		}
	}

	/**
	 * Used for owner player only
	 */
	public void updateAppearance() {
		PacketSendUtility.broadcastPacket(getOwner(), new SM_HOUSE_UPDATE(getOwner()));
	}

	public void broadcastAppearance() {
		PacketSendUtility.broadcastPacket(getOwner(), new SM_HOUSE_RENDER(getOwner()));
	}

	public void kickVisitors(Player kicker, boolean kickFriends, boolean ownerChanged) {
		List<ZoneInfo> zoneInfo = DataManager.ZONE_DATA.getZones().get(getOwner().getWorldId());
		for (ZoneInfo info : zoneInfo) {
			if (info.getZoneTemplate().getName().name().equals(getOwner().getName())) {
				getOwner().getKnownList().forEachPlayer(player -> {
					if (player.getObjectId() == getOwner().getOwnerId())
						return;
					if (!kickFriends && kicker != null && kicker.getFriendList().getFriend(player.getObjectId()) != null)
						return;
					if (player.isInsideZone(info.getZoneTemplate().getName()))
						moveOutside(player, ownerChanged);
				});
				break;
			}
		}
		if (kicker != null) {
			if (!kickFriends) {
				PacketSendUtility.sendPacket(kicker, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_OUT_WITHOUT_FRIENDS());
			} else {
				PacketSendUtility.sendPacket(kicker, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_OUT_ALL());
			}
		}
	}

	private void moveOutside(Player player, boolean ownerChanged) {
		if (getOwner().getAddress().getExitMapId() != null) {
			HouseAddress address = getOwner().getAddress();
			TeleportService.teleportTo(player, address.getExitMapId(), address.getExitX(), address.getExitY(), address.getExitZ(), (byte) 0,
				TeleportAnimation.FADE_OUT_BEAM);
		} else {
			teleportNearHouseDoor(player, true);
		}
		if (ownerChanged)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CHANGE_OWNER());
		else
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_REQUEST_OUT());
	}

	public void teleportNearHouseDoor(Player player, boolean outsideHouse) {
		SpawnTemplate butler = getOwner().getButler().getSpawn(), relationshipCrystal = getOwner().getRelationshipCrystal().getSpawn();
		float x, y, z; // midpoint between butler and relationship crystal, since we currently have no door coordinates in templates
		byte h = PositionUtil.getHeadingTowards(getOwner().getRelationshipCrystal(), butler.getX(), butler.getY());
		h -= 30; // this is the supposed heading towards the door (crystal is right from the door, so offset direction towards butler by 90 degrees)
		x = (butler.getX() + relationshipCrystal.getX()) / 2;
		y = (butler.getY() + relationshipCrystal.getY()) / 2;
		z = Math.max(butler.getZ(), relationshipCrystal.getZ());
		if (outsideHouse) { // offset the midpoint 2.5m behind the butler, to get coords outside the house, near the door
			double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(h));
			x += (float) (Math.cos(radian) * 2.5f);
			y += (float) (Math.sin(radian) * 2.5f);
		} else {
			h += h < 60 ? 60 : -60; // opposite direction (player should look inside house)
		}
		TeleportService.teleportTo(player, getOwner().getWorldId(), getOwner().getInstanceId(), x, y, z, h, TeleportAnimation.FADE_OUT_BEAM);
	}
}
