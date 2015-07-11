package com.aionemu.gameserver.model;

/**
 * Chat types that are supported by Aion.
 *
 * @author SoulKeeper, Imaginary
 */
public enum ChatType {

	NORMAL(0x00), // Normal chat (White)
	SHOUT(0x03), // Shout chat (Orange)
	WHISPER(0x04), // Whisper chat (Green)
	GROUP(0x05), // Group chat (Blue)
	ALLIANCE(0x06), // Alliance chat (Aqua)
	GROUP_LEADER(0x07), // Group Leader chat (???)
	LEAGUE(0x08), // League chat (Dark Blue)
	LEAGUE_ALERT(0x09), // League chat (Orange)
	LEGION(0x0A), // Legion chat (Green)
	COMMAND(0x18), // Command chat (Yellow)

	CH1(0x0E),
	CH2(0x0F),
	CH3(0x10),
	CH4(0x11),
	CH5(0x12),
	CH6(0x13),
	CH7(0x14),
	CH8(0x15),
	CH9(0x16),
	CH10(0x17),

	/**
	 * Global chat types
	 */
	GOLDEN_YELLOW(0x19, true), // System message (Dark Yellow), most commonly used, no "center" equivalent.

	WHITE(0x1F, true), // System message (White), visible in "All" chat thumbnail only !
	YELLOW(0x20, true), // System message (Yellow), visible in "All" chat thumbnail only !
	BRIGHT_YELLOW(0x21, true), // System message (Light Yellow), visible in "All" chat thumbnail only !
	
	WHITE_CENTER(0x22, true), // Periodic Notice (White && Box on screen center)
	YELLOW_CENTER(0x23, true), // Periodic Announcement(Yellow && Box on screen center)
	BRIGHT_YELLOW_CENTER(0x24, true); // System Notice (Light Yellow && Box on screen center)
	
	private final int intValue;
	private boolean sysMsg;

	/**
	 * Constructor client chat type integer representation
	 */
	private ChatType(int intValue) {
		this(intValue, false);
	}

	/**
	 * Converts ChatType value to integer representation
	 *
	 * @return chat type in client
	 */
	public int toInteger() {
		return intValue;
	}

	/**
	 * Returns ChatType by it's integer representation
	 *
	 * @param integerValue integer value of chat type
	 * @return ChatType
	 * @throws IllegalArgumentException if can't find suitable chat type
	 */
	public static ChatType getChatTypeByInt(int integerValue) throws IllegalArgumentException {
		for (ChatType ct : ChatType.values()) {
			if (ct.toInteger() == integerValue) {
				return ct;
			}
		}

		throw new IllegalArgumentException("Unsupported chat type: " + integerValue);
	}

	private ChatType(int intValue, boolean sysMsg) {
		this.intValue = intValue;
		this.sysMsg = sysMsg;
	}

	/**
	 * @return true if this is one of system message ( all races can read chat )
	 */
	public boolean isSysMsg() {
		return sysMsg;
	}

}
