package com.aionemu.gameserver.model.instance.instancereward;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.instance.playerreward.DredgionPlayerReward;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public class DredgionReward extends AbstractInstancePointsReward<DredgionPlayerReward> {

	@SuppressWarnings("unused")
	private int drawPoints;
	private List<DredgionRooms> dredgionRooms = new ArrayList<>();
	private Point3D asmodiansStartPosition, elyosStartPosition;

	public DredgionReward(int mapId) {
		super(mapId == 300110000 ? 3000 : 4500, mapId == 300110000 ? 1500 : 2500);
		drawPoints = mapId == 300110000 ? 2250 : 3750;
		setStartPositions();
		for (int i = 1; i < 15; i++) {
			dredgionRooms.add(new DredgionRooms(i));
		}
	}

	private void setStartPositions() {
		Point3D a = new Point3D(570.468f, 166.897f, 432.28986f);
		Point3D b = new Point3D(400.741f, 166.713f, 432.290f);

		if (Rnd.nextBoolean()) {
			asmodiansStartPosition = a;
			elyosStartPosition = b;
		} else {
			asmodiansStartPosition = b;
			elyosStartPosition = a;
		}
	}

	public void portToPosition(Player player, WorldMapInstance instance) {
		if (player.getRace() == Race.ASMODIANS) {
			TeleportService.teleportTo(player, instance, asmodiansStartPosition.getX(), asmodiansStartPosition.getY(), asmodiansStartPosition.getZ());
		} else {
			TeleportService.teleportTo(player, instance, elyosStartPosition.getX(), elyosStartPosition.getY(), elyosStartPosition.getZ());
		}
	}

	/**
	 * 1 Primary Armory 2 Backup Armory 3 Gravity Control 4 Engine Room 5 Auxiliary Power 6 Weapons Deck 7 Lower Weapons Deck 8 Ready Room 1 9 Ready
	 * Room 2 10 Barracks 11 Logistics Managment 12 Logistics Storage 13 The Bridge 14 Captain's Room
	 */
	public class DredgionRooms {

		private int roomId;
		private int state = 0xFF;

		public DredgionRooms(int roomId) {
			this.roomId = roomId;
		}

		public int getRoomId() {
			return roomId;
		}

		public void captureRoom(Race race) {
			state = race == Race.ASMODIANS ? 0x01 : 0x00;
		}

		public int getState() {
			return state;
		}
	}

	public List<DredgionRooms> getDredgionRooms() {
		return dredgionRooms;
	}

	public DredgionRooms getDredgionRoomById(int roomId) {
		for (DredgionRooms dredgionRoom : dredgionRooms) {
			if (dredgionRoom.getRoomId() == roomId) {
				return dredgionRoom;
			}
		}
		return null;
	}

	@Override
	public void clear() {
		super.clear();
		dredgionRooms.clear();
	}
}
