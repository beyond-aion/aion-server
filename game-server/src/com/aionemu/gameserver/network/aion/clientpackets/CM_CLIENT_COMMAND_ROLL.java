package com.aionemu.gameserver.network.aion.clientpackets;

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

	public CM_CLIENT_COMMAND_ROLL(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		maxRoll = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		roll = Rnd.get(1, maxRoll);
		PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1400126, roll, maxRoll));
		PacketSendUtility.broadcastPacket(player, new SM_SYSTEM_MESSAGE(1400127, player.getName(), roll, maxRoll));
	}
}
