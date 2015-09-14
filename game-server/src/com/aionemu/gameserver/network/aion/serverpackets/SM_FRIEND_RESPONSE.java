package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Replies to a request to add or delete a friend
 * 
 * @author Ben
 */
public class SM_FRIEND_RESPONSE extends AionServerPacket {

	/**
	 * The friend was successfully added to your list
	 */
	public static final int TARGET_ADDED = 0x00;
	/**
	 * The target of a friend request is offline
	 */
	public static final int TARGET_OFFLINE = 0x01;
	/**
	 * The target is already a friend
	 */
	public static final int TARGET_ALREADY_FRIEND = 0x02;
	/**
	 * The target does not exist
	 */
	public static final int TARGET_NOT_FOUND = 0x03;
	/**
	 * The friend denied your request to add him
	 */
	public static final int TARGET_DENIED = 0x04;
	/**
	 * The target's friend list is full
	 */
	public static final int TARGET_LIST_FULL = 0x05;
	/**
	 * The friend was removed from your list
	 */
	public static final int TARGET_REMOVED = 0x06;
	/**
	 * The target is in your blocked list, and cannot be added to your friends list.
	 */
	public static final int TARGET_BLOCKED = 0x08;
	/**
	 * The target is dead and cannot be befriended yet.
	 */
	public static final int TARGET_DEAD = 0x09;

	public static final int FRIEND_DENIED = 0x11;

	private final String player;
	private final int code;

	public SM_FRIEND_RESPONSE(String playerName, int messageType) {
		player = playerName;
		code = messageType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {

		writeS(player);
		writeC(code);
	}

}
