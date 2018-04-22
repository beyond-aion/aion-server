package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HousePermissions;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Rolandas, Neon
 */
public class CM_HOUSE_OPEN_DOOR extends AionClientPacket {

	int address;
	boolean leave = false;

	public CM_HOUSE_OPEN_DOOR(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		address = readD();
		if (readUC() != 0)
			leave = true;
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;

		House house = HousingService.getInstance().getHouseByAddress(address);
		if (house == null)
			return;

		if (leave) {
			if (house.getAddress().getExitMapId() != null) {
				TeleportService.teleportTo(player, house.getAddress().getExitMapId(), house.getAddress().getExitX(), house.getAddress().getExitY(),
					house.getAddress().getExitZ(), (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
			} else {
				teleportNearHouseDoor(player, house, true);
			}
		} else {
			if (player.hasAccess(AdminConfig.HOUSE_SHOW_ADDRESS))
				PacketSendUtility.sendMessage(player, "House address: " + address);
			if (house.getOwnerId() != player.getObjectId() && !player.hasAccess(AdminConfig.HOUSE_ENTER_ALL)) {
				boolean allowed = false;
				if (house.getDoorState() == HousePermissions.DOOR_OPENED_FRIENDS) {
					allowed = player.getFriendList().getFriend(house.getOwnerId()) != null
						|| (player.getLegion() != null && player.getLegion().isMember(house.getOwnerId()));
				}
				if (!allowed) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_ENTER_NO_RIGHT2());
					return;
				}
			}
			teleportNearHouseDoor(player, house, false);
		}
	}

	private static void teleportNearHouseDoor(Player player, House house, boolean outsideHouse) {
		SpawnTemplate butler = house.getButler().getSpawn(), relationshipCrystal = house.getRelationshipCrystal().getSpawn();
		float x, y, z; // midpoint between butler and relationship crystal, since we currently have no door coordinates in templates
		byte h = relationshipCrystal.getHeading(); // crystals always looks away from the door, inside the house (butlers sometimes stand diagonally)
		x = (butler.getX() + relationshipCrystal.getX()) / 2;
		y = (butler.getY() + relationshipCrystal.getY()) / 2;
		z = Math.max(butler.getZ(), relationshipCrystal.getZ());
		if (outsideHouse) { // offset the midpoint 2.5m behind the butler, to get coords outside the house, near the door
			double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(h));
			x -= (float) (Math.cos(radian) * 2.5f);
			y -= (float) (Math.sin(radian) * 2.5f);
			h -= h >= 60 ? 60 : -60; // opposite direction (player should look away from the door)
		}
		TeleportService.teleportTo(player, house.getWorldId(), x, y, z, h, TeleportAnimation.FADE_OUT_BEAM);
	}
}
