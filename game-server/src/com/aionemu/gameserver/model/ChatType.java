package com.aionemu.gameserver.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Chat types that are supported by Aion.
 *
 * @author SoulKeeper, Imaginary, Neon
 */
public enum ChatType {

	NORMAL(0), // [MT_SAY] Normal chat (White)
	NPC(1), // [MT_THINK] Npc chat (Light Blue)
	SHOUT(3), // [MT_SHOUT] Shout chat (Orange)
	WHISPER(4), // [MT_WHISPER] Whisper chat (Green)
	GROUP(5), // [MT_PARTY] Group chat (Blue)
	ALLIANCE(6), // [MT_ALLIANCE] Alliance chat (Aqua)
	GROUP_LEADER(7), // [MT_ALERT] Group Leader chat (Orange)
	LEAGUE(8), // [MT_UNION] League chat (Dark Blue)
	LEAGUE_ALERT(9), // [MT_UNIONALERT] League chat (Orange)
	LEGION(10), // [MT_GUILD] Legion chat (Green)
	// (11), // [MT_GUILD_LORD]
	// (12), // [MT_GLOBAL_CHAT]
	// (13), // [MT_TRADE_CHAT]
	CH1(14), // [MT_CHANNEL_0]
	CH2(15), // [MT_CHANNEL_1]
	CH3(16), // [MT_CHANNEL_2]
	CH4(17), // [MT_CHANNEL_3]
	CH5(18), // [MT_CHANNEL_4]
	CH6(19), // [MT_CHANNEL_5]
	CH7(20), // [MT_CHANNEL_6]
	CH8(21), // [MT_CHANNEL_7]
	CH9(22), // [MT_CHANNEL_8]
	CH10(23), // [MT_CHANNEL_9]

	COMMAND(24), // [MT_RANKER_CHAT] Command chat (Yellow), usable by commanders and supreme commanders via /c

	/**
	 * Global chat types
	 */
	GOLDEN_YELLOW(25, true), // [MT_SYSMSG_HIGH_PRI] System message (Dark Yellow), most commonly used, no "center" equivalent.
	// (26), // [MT_SYSMSG_LOW_PRI]
	GM_CHAT(27, false), // [MT_SYSMSG_PETITION] Message used in petition/support packet (S_PETITION_STATUS opcode 239), has its own window and icon next to skill bar and is used to communicate with a gm.
	// (28), // [MT_SYSMSG_MSGBOX]
	// (29), // [MT_NOTICEBOX]
	WHITE(31, true), // [MT_GMMSG_NORMAL_LEVEL_1] System message (White), visible in "All" chat thumbnail only !
	YELLOW(32, true), // [MT_GMMSG_NORMAL_LEVEL_2] System message (Yellow), visible in "All" chat thumbnail only !
	BRIGHT_YELLOW(33, true), // [MT_GMMSG_NORMAL_LEVEL_3] System message (Light Yellow), visible in "All" chat thumbnail only !

	WHITE_CENTER(34, true), // [MT_GMMSG_HIGH_LEVEL_1] Periodic Notice (White && Box on screen center)
	YELLOW_CENTER(35, true), // [MT_GMMSG_HIGH_LEVEL_2] Periodic Announcement (Yellow && Box on screen center)
	BRIGHT_YELLOW_CENTER(36, true); // [MT_GMMSG_HIGH_LEVEL_3] System Notice (Light Yellow && Box on screen center)

	private static final Map<Byte, ChatType> chatTypes = new HashMap<>();
	private final byte id;
	private final boolean sysMsg;

	static {
		for (ChatType ct : values())
			chatTypes.put(ct.getId(), ct);
	}

	/**
	 * Constructor client chat type integer representation
	 */
	private ChatType(int id) {
		this(id, false);
	}

	private ChatType(int id, boolean sysMsg) {
		this.id = (byte) id;
		this.sysMsg = sysMsg;
	}

	/**
	 * Converts ChatType value to integer representation
	 *
	 * @return chat type in client
	 */
	public byte getId() {
		return id;
	}

	/**
	 * @return true if this is a system message chat type (all races can read chat)
	 */
	public boolean isSysMsg() {
		return sysMsg;
	}

	/**
	 * Returns ChatType by it's id
	 *
	 * @param integerValue
	 *          integer value of chat type
	 * @return ChatType
	 * @throws IllegalArgumentException
	 *           if can't find suitable chat type
	 */
	public static ChatType getChatType(byte id) throws IllegalArgumentException {
		ChatType ct = chatTypes.get(id);
		if (ct == null)
			throw new IllegalArgumentException("Unsupported chat type: " + (id & 0xFF));
		return ct;
	}
}
