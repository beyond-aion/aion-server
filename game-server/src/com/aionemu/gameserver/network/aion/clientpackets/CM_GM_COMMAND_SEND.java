package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;
import java.util.regex.Pattern;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;

/**
 * @author ginho1
 */
public class CM_GM_COMMAND_SEND extends AionClientPacket {

	public static final String UNSUPPORTED_COMMAND_CHAR_PLACEHOLDER = "?"; // client sends this for each unsupported char in the command
	private static final Pattern unsupportedCommandChars = Pattern.compile("[^\u0000-\u013E]");
	private String command;

	public CM_GM_COMMAND_SEND(int opcode, Set<State> validStates) {
		super(opcode, validStates);
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

	public static String replaceUnsupportedCommandChars(String input) {
		return unsupportedCommandChars.matcher(input).replaceAll(UNSUPPORTED_COMMAND_CHAR_PLACEHOLDER);
	}
}
