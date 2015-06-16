package com.aionemu.gameserver.utils.chathandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author KID
 */
public abstract class ChatCommand {

	private String alias;
	private Byte level;
	static final String[] EMPTY_PARAMS = new String[] {};
	static final Logger log = LoggerFactory.getLogger(ChatCommand.class);
	
	public ChatCommand(String alias) {
		this.alias = alias;
	}

	public boolean run(Player player, String... params) {
		try {
			execute(player, params);
			return true;
		}
		catch (Exception e) {
			log.error("",e);
			onFail(player, e.getMessage());
			return false;
		}
	}

	public final String getAlias() {
		return alias;
	}

	public void setAccessLevel(Byte level) {
		this.level = level;
	}

	public final Byte getLevel() {
		return level;
	}

	abstract boolean checkLevel(Player player);

	abstract boolean process(Player player, String text);

	public abstract void execute(Player player, String... params);

	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, message);
	}
}
