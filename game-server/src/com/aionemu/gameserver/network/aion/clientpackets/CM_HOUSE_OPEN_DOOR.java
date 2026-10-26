package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * This packet is sent when clicking on a house door. Whether you want to get in or out is decided by the client.
 * 
 * @author Rolandas, Neon
 */
public class CM_HOUSE_OPEN_DOOR extends AionClientPacket {

	/**
	 * Distance from the house center that covers the door from outside and the whole interior of the largest houses.
	 */
	private static final float MAX_DISTANCE_TO_HOUSE = 40;
	private int address;
	private boolean leave;

	public CM_HOUSE_OPEN_DOOR(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		address = readD();
		leave = readC() != 0;
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;

		House house = HousingService.getInstance().getHouseByAddress(address);
		if (house == null)
			return;

		if (!isAtHouse(player, house)) {
			AuditLogger.log(player, "tried to use the door of house " + address + " from " + player.getPosition());
			return;
		}
		if (leave) {
			if (house.getAddress().getExitMapId() != null) {
				TeleportService.teleportTo(player, house.getAddress().getExitMapId(), house.getAddress().getExitX(), house.getAddress().getExitY(),
					house.getAddress().getExitZ(), (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
			} else {
				house.getController().teleportNearHouseDoor(player, true);
			}
		} else {
			if (player.hasAccess(AdminConfig.HOUSE_SHOW_ADDRESS))
				PacketSendUtility.sendMessage(player, "House address: " + address);
			if (!house.canEnter(player)) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_ENTER_NO_RIGHT2());
				return;
			}
			house.getController().teleportNearHouseDoor(player, false);
		}
	}

	private boolean isAtHouse(Player player, House house) {
		if (house.getAddress().getExitMapId() != null) // studios share one address, any studio instance of the map may leave through it
			return player.getWorldId() == house.getAddress().getMapId();
		return house.isSpawned() && PositionUtil.isInRange(player, house, MAX_DISTANCE_TO_HOUSE);
	}
}
