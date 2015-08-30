package com.aionemu.gameserver.network.aion.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Message [chat, etc]
 * 
 * @author -Nemesiss-, Sweetkr
 * @modified Neon
 */
public class SM_MESSAGE extends AionServerPacket {

	static final Logger log = LoggerFactory.getLogger(SM_MESSAGE.class);

	/**
	 * Maximum valid message length for a single chat message packet.<br>
	 * The client can't handle more than 4000 chars in one packet.<br>
	 * 4001+ disables all further client chat processing, ~4500 triggers send log error.
	 */
	public static final short MESSAGE_SIZE_LIMIT = 4000;

	/**
	 * Maximum valid message line count for a single chat message packet.<br>
	 * The client does not always correctly display more than this number of lines.
	 */
	public static final short MESSAGE_LINE_LIMIT = 15;

	/**
	 * ID of the object that is saying something or <tt>null</tt>.
	 */
	private int senderObjectId;

	/**
	 * Message
	 */
	private String message;

	/**
	 * Name of the sender
	 */
	private String senderName;

	/**
	 * Filter that the client will apply for the message.<br>
	 * If the readers race does not match the senders race, he will receive an obfuscated message.<br>
	 * 0: all, 1: elyos, 2: asmodian
	 */
	private byte senderRace;

	/**
	 * Chat type
	 */
	private ChatType chatType;

	/**
	 * Sender coordinates
	 */
	private float x;
	private float y;
	private float z;

	/**
	 * Constructs new <tt>SM_MESSAGE</tt> packet.
	 * 
	 * @param sender
	 *          player who sent the message
	 * @param message
	 *          actual message
	 * @param chatType
	 *          what chat type should be used
	 */
	public SM_MESSAGE(Player sender, String message, ChatType chatType) {
		this(sender, sender.getObjectId(), sender.getName(AdminConfig.CUSTOMTAG_ENABLE), message, chatType);
	}

	/**
	 * Constructs new <tt>SM_MESSAGE</tt> packet.
	 * 
	 * @param sender
	 *          npc who sent the message
	 * @param message
	 *          actual message
	 * @param chatType
	 *          what chat type should be used
	 */
	public SM_MESSAGE(Npc sender, String message, ChatType chatType) {
		this(sender, sender.getObjectId(), sender.getName(), message, chatType);
	}

	/**
	 * Manual creation of chat message.
	 * 
	 * @param senderObjectId
	 *          can be 0 for system message (like announcements)
	 * @param senderName
	 *          used for shout ATM, can be null in other cases
	 * @param message
	 *          actual message
	 * @param chatType
	 *          what chat type should be used
	 */
	public SM_MESSAGE(int senderObjectId, String senderName, String message, ChatType chatType) {
		this(null, senderObjectId, senderName, message, chatType);
	}

	private SM_MESSAGE(Creature sender, int senderObjectId, String senderName, String message, ChatType chatType) {
		int lines = message.split("\n", -1).length;
		if (lines > MESSAGE_LINE_LIMIT) {
			log.warn("Exceeded maximum line number for packet SM_MESSAGE.\nLines: " + lines + "\nMessage: " + message);
		}
		if (message.length() > MESSAGE_SIZE_LIMIT) {
			log.warn("Exceeded maximum string size for packet SM_MESSAGE.\nSize: " + message.length() + "\nMessage: " + message);
			message = message.substring(0, MESSAGE_SIZE_LIMIT); // shorten message to avoid send log error
		}
		if (sender != null) {
			if (sender instanceof Player) {
				if (!(chatType.isSysMsg() || CustomConfig.SPEAKING_BETWEEN_FACTIONS || ((Player) sender).getAccessLevel() > 0)) {
					this.senderRace = (byte) (((Player) sender).getRace().getRaceId() + 1);
				}
			}
			this.x = sender.getX();
			this.y = sender.getY();
			this.z = sender.getZ();
		}
		this.senderObjectId = senderObjectId;
		this.senderName = senderName;
		this.message = message;
		this.chatType = chatType;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(chatType.toInteger());
		writeC((senderRace > 0 && con.getActivePlayer() != null && con.getActivePlayer().getAccessLevel() > 0) ? 0 : senderRace);
		writeD(senderObjectId);

		switch (chatType) {
			case NORMAL:
			case GOLDEN_YELLOW:
			case WHITE:
			case YELLOW:
			case BRIGHT_YELLOW:
			case WHITE_CENTER:
			case YELLOW_CENTER:
			case BRIGHT_YELLOW_CENTER:
				writeH(0x00); // unknown
				writeS(message);
				break;
			case SHOUT:
				writeS(senderName);
				writeS(message);
				writeF(x);
				writeF(y);
				writeF(z);
				break;
			case ALLIANCE:
			case GROUP:
			case GROUP_LEADER:
			case LEGION:
			case WHISPER:
			case LEAGUE:
			case LEAGUE_ALERT:
			case CH1:
			case CH2:
			case CH3:
			case CH4:
			case CH5:
			case CH6:
			case CH7:
			case CH8:
			case CH9:
			case CH10:
			case COMMAND:
				writeS(senderName);
				writeS(message);
				break;
		}
	}
}
