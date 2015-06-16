package com.aionemu.loginserver.network.aion;

/**
 * This class contains possible response that LoginServer may send to client if login fail etc.
 * 
 * @author KID
 */
public enum AionAuthResponse {
	/**
	 * that one is not being sent to client, it's only for internal use. Everything is OK
	 */
	AUTHED(0),
	/**
	 * System error.
	 */
	SYSTEM_ERROR(1),
	/**
	 * ID or password does not match.
	 */
	INVALID_PASSWORD(2),
	/**
	 * ID or password does not match.
	 */
	INVALID_PASSWORD2(3),
	/**
	 * Failed to load your account info.
	 */
	FAILED_ACCOUNT_INFO(4),
	/**
	 * Failed to load your social security number.
	 */
	FAILED_SOCIAL_NUMBER(5),
	/**
	 * No game server is registered to the authorization server.
	 */
	NO_GS_REGISTERED(6),
	/**
	 * You are already logged in.
	 */
	ALREADY_LOGGED_IN(7),
	/**
	 * The selected server is down and you cannot access it.
	 */
	SERVER_DOWN(8),
	/**
	 * The login information does not match the information you provided.
	 */
	INVALID_PASSWORD3(9),
	/**
	 * No Login info available.
	 */
	NO_SUCH_ACCOUNT(10),
	/**
	 * You have been disconnected from the server. Please try connecting again later.
	 */
	DISCONNECTED(11),
	/**
	 * You are not old enough to play the game.
	 */
	AGE_LIMIT(12),
	/**
	 * Double login attempts have been detected.
	 */
	ALREADY_LOGGED_IN2(13),
	/**
	 * You are already logged in.
	 */
	ALREADY_LOGGED_IN3(14),
	/**
	 * You cannot connect to the server because there are too many users right now.
	 */
	SERVER_FULL(15),
	/**
	 * The server is being normalized. Please try connecting again later.
	 */
	GM_ONLY(16),
	/**
	 * Please login to the game after you have changed your password.
	 */
	ERROR_17(17),
	/**
	 * You have used all your allowed playing time.
	 */
	TIME_EXPIRED(18),
	/**
	 * You have used up your allocated time and there is no time left on this account.
	 */
	TIME_EXPIRED2(19),
	/**
	 * System error.
	 */
	SYSTEM_ERROR2(20),
	/**
	 * The IP is already in use.
	 */
	ALREADY_USED_IP(21),
	/**
	 * You cannot access the game through this IP.
	 */
	BAN_IP(22);

	/**
	 * id of this enum that may be sent to client
	 */
	private int messageId;

	/**
	 * Constructor.
	 * 
	 * @param msgId
	 *          id of the message
	 */
	private AionAuthResponse(int msgId) {
		messageId = msgId;
	}

	/**
	 * Message Id that may be sent to client.
	 * 
	 * @return message id
	 */
	public int getMessageId() {
		return messageId;
	}
}
