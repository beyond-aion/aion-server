package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CHAT_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author prix
 */
public class CM_CHAT_PLAYER_INFO extends AionClientPacket {

	private String playerName;

	public CM_CHAT_PLAYER_INFO(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		playerName = readS();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Player target = World.getInstance().findPlayer(playerName);
		if (target == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_ASK_PCINFO_LOGOFF);
			return;
		}
		if(!player.getKnownList().knowns(target))
			PacketSendUtility.sendPacket(player, new SM_CHAT_WINDOW(target, false));
	}
}
	
