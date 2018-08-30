package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rhys2002
 */
public class CM_CLIENT_COMMAND_ROLL extends AionClientPacket {

	private int maxRoll;
	private int roll;

	public CM_CLIENT_COMMAND_ROLL(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		maxRoll = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		roll = Rnd.get(1, maxRoll);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DICE_CUSTOM_ME(roll, maxRoll));
		PacketSendUtility.broadcastPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DICE_CUSTOM_OTHER(player.getName(), roll, maxRoll));
	}
}
