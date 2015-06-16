package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;

/**
 * @author ginho1
 *
 */
public class CM_GM_COMMAND_SEND extends AionClientPacket {

	public String Command;

	/**
	 * @param opcode
	 * @param state
	 * @param restStates
	 */
	public CM_GM_COMMAND_SEND(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/* (non-Javadoc)
	 * @see com.aionemu.commons.network.packet.BaseClientPacket#readImpl()
	 */
	@Override
	protected void readImpl() {
		Command = readS();

	}

	/* (non-Javadoc)
	 * @see com.aionemu.commons.network.packet.BaseClientPacket#runImpl()
	 */
	@Override
	protected void runImpl() {
		Player admin = getConnection().getActivePlayer();
		if (admin.getAccessLevel() < AdminConfig.GM_PANEL)
			return;
		ChatProcessor.getInstance().handleConsoleCommand(admin, Command);
	}

}