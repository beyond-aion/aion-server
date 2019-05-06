package com.aionemu.gameserver.controllers;

import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HouseType;
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

	public void kickVisitors(Player kicker, boolean kickFriends, boolean onSettingsChange) {
		List<ZoneInfo> zoneInfo = DataManager.ZONE_DATA.getZones().get(getOwner().getWorldId());
		for (ZoneInfo info : zoneInfo) {
			if (info.getZoneTemplate().getName().name().equals(getOwner().getName())) {
				getOwner().getKnownList().forEachPlayer(player -> {
					if (player.getObjectId() == getOwner().getOwnerId())
						return;
					if (!kickFriends && kicker != null && kicker.getFriendList().getFriend(player.getObjectId()) != null)
						return;
					if (player.isInsideZone(info.getZoneTemplate().getName()))
						moveOutside(player, onSettingsChange);
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

	private void moveOutside(Player player, boolean onSettingsChange) {
		if (getOwner().getHouseType() == HouseType.STUDIO) {
			float x = getOwner().getAddress().getExitX();
			float y = getOwner().getAddress().getExitY();
			float z = getOwner().getAddress().getExitZ();
			TeleportService.teleportTo(player, getOwner().getAddress().getExitMapId(), 1, x, y, z, player.getHeading(), TeleportAnimation.FADE_OUT_BEAM);
		} else {
			Npc sign = getOwner().getCurrentSign();
			double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(sign.getHeading()));
			float x = (float) (sign.getX() + (8 * Math.cos(radian)));
			float y = (float) (sign.getY() + (8 * Math.sin(radian)));
			TeleportService.teleportTo(player, getOwner().getWorldId(), 1, x, y, player.getZ() + 1, player.getHeading(), TeleportAnimation.FADE_OUT_BEAM);
		}
		if (onSettingsChange)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CHANGE_OWNER());
		else
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_REQUEST_OUT());
	}
}
