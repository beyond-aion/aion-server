package com.aionemu.gameserver.model;

/**
 * This class represents an announcement
 * 
 * @author Divinity
 */
public class Announcement {

	private final int id;
	private final Race faction;
	private final String announce;
	private final String chatType;
	private final int delay;

	/**
	 * Constructor with the ID of announcement
	 * 
	 * @param id
	 * @param announce
	 * @param faction
	 * @param chatType
	 * @param delay
	 */
	public Announcement(int id, String announce, String faction, String chatType, int delay) {
		this.id = id;
		this.announce = announce;
		this.faction = getFactionEnum(faction);
		this.chatType = chatType;
		this.delay = delay;
	}

	private Race getFactionEnum(String faction) {
		if (faction.equalsIgnoreCase("ELYOS"))
			return Race.ELYOS;
		else if (faction.equalsIgnoreCase("ASMODIANS"))
			return Race.ASMODIANS;
		return null;
	}

	/**
	 * Return the id of the announcement
	 * 
	 * @return int - Announcement's id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Return the announcement's text
	 * 
	 * @return String - Announcement's text
	 */
	public String getAnnounce() {
		return announce;
	}

	/**
	 * Return the announcement's faction (ELYOS or ASMODIANS, null if unrestricted)
	 * 
	 * @return Announcement's faction
	 */
	public Race getFaction() {
		return faction;
	}

	/**
	 * Return the chatType in String mode (for the insert in database)
	 * 
	 * @return String - Announcement's chatType
	 */
	public String getType() {
		return chatType;
	}

	/**
	 * Return the chatType with the ChatType Enum
	 * 
	 * @return ChatType - Announcement's chatType
	 */
	public ChatType getChatType() {
		if (chatType.equalsIgnoreCase("System"))
			return ChatType.GOLDEN_YELLOW;
		else if (chatType.equalsIgnoreCase("White"))
			return ChatType.WHITE_CENTER;
		else if (chatType.equalsIgnoreCase("Yellow"))
			return ChatType.YELLOW_CENTER;
		else if (chatType.equalsIgnoreCase("Shout"))
			return ChatType.SHOUT;
		else if (chatType.equalsIgnoreCase("Orange"))
			return ChatType.GROUP_LEADER;
		else
			return ChatType.BRIGHT_YELLOW_CENTER;
	}

	/**
	 * Return the announcement's delay
	 * 
	 * @return int - Announcement's delay
	 */
	public int getDelay() {
		return delay;
	}
}
