package com.aionemu.gameserver.network.aion.clientpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.AbstractHouseInfoPacket.SIGN_NOTICE_MAX_LENGTH;

import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HouseDoorState;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_ACQUIRE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Rolandas
 */
public class CM_HOUSE_SETTINGS extends AionClientPacket {

	private byte doorState;
	private boolean showOwnerName;
	private String signNotice;

	public CM_HOUSE_SETTINGS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		doorState = readC();
		showOwnerName = readC() == 1;
		signNotice = readS();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;

		if (signNotice.length() > SIGN_NOTICE_MAX_LENGTH) { // client limits sign notices to 64 chars but technically it supports more
			AuditLogger.log(player, "sent string with more than 64 chars for house notice: " + signNotice);
			signNotice = signNotice.substring(0, SIGN_NOTICE_MAX_LENGTH);
		}
		HouseDoorState doorState = HouseDoorState.get(this.doorState);
		House house = player.getActiveHouse();
		if (doorState == null)
			LoggerFactory.getLogger(getClass()).warn("{} sent unknown door state {} for {}", player, this.doorState, house);
		else
			house.setDoorState(doorState);
		house.setShowOwnerName(showOwnerName);
		house.setSignNotice(signNotice);

		sendPacket(new SM_HOUSE_ACQUIRE(player.getObjectId(), house.getAddress().getId(), true));
		house.getController().updateAppearance();

		if (doorState == HouseDoorState.OPEN)
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_OPEN_DOOR());
		else if (doorState == HouseDoorState.CLOSED_EXCEPT_FRIENDS) {
			house.getController().kickVisitors(player, false, false);
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_CLOSE_DOOR_WITHOUT_FRIENDS());
		} else if (doorState == HouseDoorState.CLOSED) {
			house.getController().kickVisitors(player, true, false);
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_CLOSE_DOOR_ALL());
		}
	}

}
