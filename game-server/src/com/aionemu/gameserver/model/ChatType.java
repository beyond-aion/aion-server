package com.aionemu.gameserver.model;

import java.util.Map;

import javolution.util.FastMap;

/**
 * Chat types that are supported by Aion.
 *
 * @author SoulKeeper, Imaginary, Neon
 */
public enum ChatType {

	NORMAL(0), // Normal chat (White)
	NPC(1), // Npc chat (Light Blue)
	SHOUT(3), // Shout chat (Orange)
	WHISPER(4), // Whisper chat (Green)
	GROUP(5), // Group chat (Blue)
	ALLIANCE(6), // Alliance chat (Aqua)
	GROUP_LEADER(7), // Group Leader chat (Orange)
	LEAGUE(8), // League chat (Dark Blue)
	LEAGUE_ALERT(9), // League chat (Orange)
	LEGION(10), // Legion chat (Green)

	CH1(14),
	CH2(15),
	CH3(16),
	CH4(17),
	CH5(18),
	CH6(19),
	CH7(20),
	CH8(21),
	CH9(22),
	CH10(23),

	COMMAND(24), // Command chat (Yellow)

	/**
	 * Global chat types
	 */
	GOLDEN_YELLOW(25, true), // System message (Dark Yellow), most commonly used, no "center" equivalent.

	WHITE(31, true), // System message (White), visible in "All" chat thumbnail only !
	YELLOW(32, true), // System message (Yellow), visible in "All" chat thumbnail only !
	BRIGHT_YELLOW(33, true), // System message (Light Yellow), visible in "All" chat thumbnail only !

	WHITE_CENTER(34, true), // Periodic Notice (White && Box on screen center)
	YELLOW_CENTER(35, true), // Periodic Announcement(Yellow && Box on screen center)
	BRIGHT_YELLOW_CENTER(36, true); // System Notice (Light Yellow && Box on screen center)

	private static Map<Byte, ChatType> chatTypes = new FastMap<Byte, ChatType>().shared();
	private byte id;
	private boolean sysMsg;

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
