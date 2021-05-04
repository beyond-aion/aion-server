package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Received when a player types /roll in chat. {@link #maxRoll} is an optional numeric parameter which defaults to 100 on client side.
 * 
 * @author Rhys2002
 */
public class CM_CLIENT_COMMAND_ROLL extends AionClientPacket {

	private int maxRoll;

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
		if (maxRoll <= 0) // client sends 100 on /roll 0 but negative numbers are passed through for whatever reason
			maxRoll = 100;
		int roll = Rnd.get(1, maxRoll);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DICE_CUSTOM_ME(roll, maxRoll));
		PacketSendUtility.broadcastPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DICE_CUSTOM_OTHER(player.getName(), roll, maxRoll));
	}
}
