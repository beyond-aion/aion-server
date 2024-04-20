package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CHAT_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.world.World;

/**
 * @author prix, Neon
 */
public class CM_CHAT_PLAYER_INFO extends AionClientPacket {

	private String playerName;

	public CM_CHAT_PLAYER_INFO(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		playerName = readS();
	}

	@Override
	protected void runImpl() {
		Player target = World.getInstance().getPlayer(ChatUtil.getRealCharName(playerName));
		if (target == null) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(playerName));
			return;
		}
		if (!getConnection().getActivePlayer().getKnownList().knows(target))
			sendPacket(new SM_CHAT_WINDOW(target, false));
	}
}
