package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;

/**
 * @author ginho1
 */
public class CM_GM_COMMAND_SEND extends AionClientPacket {

	public String command;

	public CM_GM_COMMAND_SEND(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		command = readS();

	}

	@Override
	protected void runImpl() {
		Player admin = getConnection().getActivePlayer();
		if (!admin.hasAccess(AdminConfig.GM_PANEL))
			return;
		ChatProcessor.getInstance().handleConsoleCommand(admin, command);
	}
}
