package com.aionemu.gameserver.controllers;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
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
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author Rolandas
 */
public class HouseController extends VisibleObjectController<House> {

	ConcurrentHashMap<Integer, ActionObserver> observed = new ConcurrentHashMap<>();

	@Override
	public void see(VisibleObject object) {
		Player p = (Player) object;
		ActionObserver observer = new ActionObserver(ObserverType.MOVE);
		p.getObserveController().addObserver(observer);
		observed.put(p.getObjectId(), observer);
		spawnObjects();
	}

	@Override
	public void notSee(VisibleObject object, ObjectDeleteAnimation animation) {
		Player p = (Player) object;
		ActionObserver observer = observed.remove(p.getObjectId());
		p.getObserveController().removeObserver(observer);
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
		ThreadPoolManager.getInstance().execute(new Runnable() {

			@Override
			public void run() {
				for (int playerId : observed.keySet()) {
					Player player = World.getInstance().findPlayer(playerId);
					if (player == null)
						continue;
					PacketSendUtility.sendPacket(player, new SM_HOUSE_UPDATE(getOwner()));
				}
			}
		});
	}

	public void broadcastAppearance() {
		ThreadPoolManager.getInstance().execute(new Runnable() {

			@Override
			public void run() {
				for (int playerId : observed.keySet()) {
					Player player = World.getInstance().findPlayer(playerId);
					if (player == null)
						continue;
					PacketSendUtility.sendPacket(player, new SM_HOUSE_RENDER(getOwner()));
				}
			}
		});
	}

	public void kickVisitors(Player kicker, boolean kickFriends, boolean onSettingsChange) {
		List<ZoneInfo> zoneInfo = DataManager.ZONE_DATA.getZones().get(getOwner().getWorldId());
		for (ZoneInfo info : zoneInfo) {
			if (info.getZoneTemplate().getName().name().equals(getOwner().getName())) {
				for (Integer objId : this.observed.keySet()) {
					if (objId == getOwner().getOwnerId())
						continue;
					if (!kickFriends && kicker != null && kicker.getFriendList().getFriend(objId) != null)
						continue;
					Player visitor = World.getInstance().findPlayer(objId);
					if (visitor != null) {
						if (visitor.isInsideZone(info.getZoneTemplate().getName())) {
							moveOutside(visitor, onSettingsChange);
						}
					}
				}
				break;
			}
		}
		if (kicker != null) {
			if (!kickFriends) {
				PacketSendUtility.sendPacket(kicker, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_OUT_WITHOUT_FRIENDS);
			} else {
				PacketSendUtility.sendPacket(kicker, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_OUT_ALL);
			}
		}
	}

	private void moveOutside(Player player, boolean onSettingsChange) {
		if (getOwner().getHouseType() == HouseType.STUDIO) {
			float x = getOwner().getAddress().getExitX();
			float y = getOwner().getAddress().getExitY();
			float z = getOwner().getAddress().getExitZ();
			TeleportService2.teleportTo(player, getOwner().getAddress().getExitMapId(), 1, x, y, z, player.getHeading(), TeleportAnimation.FADE_OUT_BEAM);
		} else {
			Npc sign = getOwner().getCurrentSign();
			double radian = Math.toRadians(MathUtil.convertHeadingToDegree(sign.getHeading()));
			float x = (float) (sign.getX() + (8 * Math.cos(radian)));
			float y = (float) (sign.getY() + (8 * Math.sin(radian)));
			TeleportService2.teleportTo(player, getOwner().getWorldId(), 1, x, y, player.getZ() + 1, player.getHeading(), TeleportAnimation.FADE_OUT_BEAM);
		}
		if (onSettingsChange)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CHANGE_OWNER);
		else
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_REQUEST_OUT);
	}
}
