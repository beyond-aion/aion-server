package com.aionemu.gameserver.network.aion.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;

/**
 * Message [chat, etc]
 * 
 * @author -Nemesiss-, Sweetkr, Neon
 */
public class SM_MESSAGE extends AionServerPacket {

	static final Logger log = LoggerFactory.getLogger(SM_MESSAGE.class);

	/**
	 * Maximum valid message length for a single chat message packet.<br>
	 * The client can't handle more than 4000 chars in one packet.<br>
	 * 4001+ disables all further client chat processing, ~4500 triggers send log error.
	 */
	public static final int MESSAGE_SIZE_HARDCAP = 4000;
	/**
	 * Maximum number of characters the client will display from a single chat message packet. Tested with a 4.8 client.
	 */
	public static final int MESSAGE_SIZE_LIMIT = 1022;

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
		this(sender, sender.getEffectController().isAbnormalSet(AbnormalState.HIDE) ? 0 : sender.getObjectId(), sender.getName(true), message, chatType);
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
	 *          if the client knows the senderObjectId, it will automatically display the objects name, otherwise this senderName will be shown (system
	 *          chats never show any sender name though)
	 * @param message
	 *          actual message
	 * @param chatType
	 *          what chat type should be used
	 */
	public SM_MESSAGE(int senderObjectId, String senderName, String message, ChatType chatType) {
		this(null, senderObjectId, senderName, message, chatType);
	}

	private SM_MESSAGE(Creature sender, int senderObjectId, String senderName, String message, ChatType chatType) {
		if (message.length() > MESSAGE_SIZE_LIMIT) {
			log.warn("Exceeded maximum string size for packet SM_MESSAGE.\nSize: " + message.length() + "\nMessage: " + message);
			if (message.length() > MESSAGE_SIZE_HARDCAP)
				message = message.substring(0, MESSAGE_SIZE_HARDCAP); // shorten message to avoid send log error
		}
		if (sender != null) {
			if (sender instanceof Player && !chatType.isSysMsg() && !CustomConfig.SPEAKING_BETWEEN_FACTIONS && !((Player) sender).isStaff()) {
				this.senderRace = (byte) (((Player) sender).getRace().getRaceId() + 1);
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
		Player activePlayer = con.getActivePlayer();
		if (activePlayer == null)
			return;
		writeC(chatType.getId());
		writeC(activePlayer.isStaff() ? 0 : senderRace);
		writeD(senderObjectId);
		writeS(senderName);
		writeS(message);
		if (chatType == ChatType.SHOUT) {
			writeF(x);
			writeF(y);
			writeF(z);
		}
	}
}
