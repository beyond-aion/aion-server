package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Replies to a request to add or delete a friend
 * 
 * @author Ben, Neon
 */
public class SM_FRIEND_RESPONSE extends AionServerPacket {

	/**
	 * You have successfully added %s to your friend list.
	 */
	public static final SM_FRIEND_RESPONSE TARGET_ADDED(String targetName) {
		return new SM_FRIEND_RESPONSE(targetName, 0x0);
	}

	/**
	 * That person is offline.
	 */
	public static final SM_FRIEND_RESPONSE TARGET_OFFLINE = new SM_FRIEND_RESPONSE(0x1);

	/**
	 * The character is already on your friend list.
	 */
	public static final SM_FRIEND_RESPONSE TARGET_ALREADY_FRIEND = new SM_FRIEND_RESPONSE(0x02);

	/**
	 * The character does not exist.
	 */
	public static final SM_FRIEND_RESPONSE TARGET_NOT_FOUND = new SM_FRIEND_RESPONSE(0x03);

	/**
	 * %s denied your request to add him.
	 */
	public static final SM_FRIEND_RESPONSE TARGET_DENIED(String targetName) {
		return new SM_FRIEND_RESPONSE(targetName, 0x04);
	}

	/**
	 * Your friend list is full.
	 */
	public static final SM_FRIEND_RESPONSE LIST_FULL = new SM_FRIEND_RESPONSE(0x05);

	/**
	 * You have removed %s from your friend list.
	 */
	public static final SM_FRIEND_RESPONSE TARGET_REMOVED(String targetName) {
		return new SM_FRIEND_RESPONSE(targetName, 0x06);
	}

	/**
	 * The target cannot be added to your friends list because he has blocked you.
	 */
	public static final SM_FRIEND_RESPONSE TARGET_BLOCKED_YOU = new SM_FRIEND_RESPONSE(0x08);

	/**
	 * The specified character is already dead.
	 */
	public static final SM_FRIEND_RESPONSE TARGET_DEAD = new SM_FRIEND_RESPONSE(0x09);

	/**
	 * The friend list of %s is full.
	 */
	public static final SM_FRIEND_RESPONSE TARGET_LIST_FULL(String targetName) {
		return new SM_FRIEND_RESPONSE(targetName, 0x0A);
	}

	/**
	 * %s is currently not online. The friend request has been sent though.
	 */
	public static final SM_FRIEND_RESPONSE TARGET_OFFLINE_SENT_REQUEST(String targetName) {
		return new SM_FRIEND_RESPONSE(targetName, 0x0B);
	}

	/**
	 * A friend request to %s exists already.
	 */
	public static final SM_FRIEND_RESPONSE TARGET_REQUESTED_ALREADY(String targetName) {
		return new SM_FRIEND_RESPONSE(targetName, 0x0C);
	}

	/**
	 * No more friend requests can be sent. Reached the maximum number of requests.
	 */
	public static final SM_FRIEND_RESPONSE TOO_MANY_REQUESTS = new SM_FRIEND_RESPONSE(0x0D);

	/**
	 * The friend list of %s is full. Accepting requests is not possible anymore.
	 */
	public static final SM_FRIEND_RESPONSE REQUESTER_LIST_FULL_CANT_ACCEPT(String targetName) {
		return new SM_FRIEND_RESPONSE(targetName, 0x0E);
	}

	/**
	 * (this closes the send friend request window without any chat notification)
	 */
	public static final SM_FRIEND_RESPONSE CLOSE_SEND_REQUEST_WINDOW = new SM_FRIEND_RESPONSE(0x11);

	/**
	 * You have denied the friend request from %s.
	 */
	public static final SM_FRIEND_RESPONSE REQUEST_DENIED(String requesterName) {
		return new SM_FRIEND_RESPONSE(requesterName, 0x12);
	}

	/**
	 * You have already received a request from %s.
	 */
	public static final SM_FRIEND_RESPONSE REQUEST_ALREADY_RECEIVED(String targetName) {
		return new SM_FRIEND_RESPONSE(targetName, 0x13);
	}

	private final String playerName;
	private final int code;

	public SM_FRIEND_RESPONSE(int messageType) {
		this("", messageType);
	}

	public SM_FRIEND_RESPONSE(String playerName, int messageType) {
		this.playerName = playerName;
		this.code = messageType;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeS(playerName);
		writeC(code);
	}
}
