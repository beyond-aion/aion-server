package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;
import java.util.regex.Pattern;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.utils.chathandlers.ChatProcessor;

public abstract class AbstractGmCommandPacket extends AionClientPacket {

	public static final String UNSUPPORTED_COMMAND_CHAR_PLACEHOLDER = "?"; // client sends this for each unsupported char in the command
	private static final Pattern unsupportedCommandChars = Pattern.compile("[^\u0000-\u013E]");
	protected String command;

	protected AbstractGmCommandPacket(int opcode, Set<AionConnection.State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		command = readS();
	}

	@Override
	protected void runImpl() {
		ChatProcessor.getInstance().handleConsoleCommand(getConnection().getActivePlayer(), command);
	}

	public static String replaceUnsupportedCommandChars(String input) {
		return unsupportedCommandChars.matcher(input).replaceAll(UNSUPPORTED_COMMAND_CHAR_PLACEHOLDER);
	}
}
