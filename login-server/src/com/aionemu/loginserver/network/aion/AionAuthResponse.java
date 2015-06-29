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
	 * You have been disconnected from the server by request of the PlayNC Homepage.
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
	 * The server is currently unavailable. Please try connecting again later.
	 */
	GM_ONLY(16),
	
	/**
	 * Please login to the game after you have changed your password.
	 */
	CHANGE_PASSWORD(17),
	
	/**
	 * Either the usage period has expired or we are experiencing a temporary connection difficulty. For
	 * more information, please contact the administrators or our customer center.
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
	BAN_IP(22),
	
	/**
	 * Deleted the character.
	 */
	CHARACTER_DELETED(23),
	
	/**
	 * Created the character.
	 */
	CHARACTER_CREATED(24),
	
	/**
	 * Invalid character name
	 */
	CHARACTER_NAME_INVALID(25),
	
	/**
	 * Invalid character info
	 */
	CHARACTER_INFO_INVALID(26),
	
	/**
	 * Invalid character info (class)
	 */
	CHARACTER_INFO_INVALID2(27),
	
	/**
	 * Invalid character attribute
	 */
	CHARACTER_ATTR_INVALID(28),
	
	/**
	 * Exceeds the maximum number of characters
	 */
	CHARACTER_LIMIT_REACHED(29),
	
	/**
	 * Used up all your relax server time
	 */
	TIME_EXPIRED3(30),
	
	/**
	 * Failed to authenticate the Security Card.
	 */
	FAILED_SECURITY_CARD(31),
	
	/**
	 * According to the Juvenile Protection Policy, you cannot play the game at this time.
	 */
	PROTECTION_POLICY(32),
	
	/**
	 * Restricted server.
	 */
	RESTRICTED_SERVER(33),
	
	/**
	 * Usage time has expired
	 */
	TIME_EXPIRED4(34),
	
	/**
	 * You cannot run 2 clients in one computer in the internet cafe.
	 */
	CLIENT_LIMIT_INTERNET_CAFE(35),
	
	/**
	 * Dormant account
	 */
	DORMANT_ACCOUNT(36),
	
	/**
	 * Your account has not been verified. Please identify yourself on the Aion Free-to-Play website
	 * (www.aionfreetoplay.com) and start again.
	 */
	NOT_VERIFIED(37),
	
	/**
	 * Waiting for parent approval.
	 */
	PARENT_APPROVAL(38),
	
	/**
	 * Waiting to leave
	 */
	WAITING_TO_LEAVE(39),
	
	/**
	 * Changed Account
	 */
	CHANGED_ACCOUNT(40),
	
	/**
	 * Request Quiz Change
	 */
	CHANGE_SECURITY_QUESTION(41),
	
	/**
	 * Exceeded the maximum number of simultaneously connected accounts for a user.
	 */
	CLIENT_LIMIT2(42),
	
	/**
	 * If you use an external account, you are blocked in the external authorization system.
	 */
	BLOCKED_EXTERNALLY(43),
	
	/**
	 * Authorization Error ()
	 */
	AUTHORIZATION_ERROR(44),
	
	/**
	 * You can only run Aion after logging in on the official homepage.
	 */
	WEBSITE_LOGIN(45),
	
	/**
	 * The authentication number is incorrect. Please check the number once again.
	 */
	PHONE_AUTH_INCORRECT(46),
	
	/**
	 * Telephone authentication service is currently under maintenance. Please try again later.
	 */
	PHONE_AUTH_MAINTENANCE(47),
	
	/**
	 * The input time for the ARS number has expired.
	 */
	ARS_NUMBER(48),
	
	/**
	 * The telephone authentication service is unavailable as there is no phone connection or the line is busy.
	 * Please try again after moving to where there is phone connection or hanging up the phone.
	 */
	PHONE_AUTH_UNAVAILABLE(49),
	
	/**
	 * The telephone number is incorrect. Please check your registration information.
	 */
	PHONE_NUMBER_INCORRECT(50),
	
	/**
	 * Telephone authentication service is currently under maintenance. Please try again later.
	 */
	PHONE_AUTH_MAINTENANCE2(51),
	
	/**
	 * The telephone authentication service is currently unavailable due to a high customer demand. Please try again later.
	 */
	PHONE_AUTH_UNAVAILABLE2(52),
	
	/**
	 * Telephone authentication service period has expired. Please try again after you have paid for the service.
	 */
	PHONE_AUTH_EXPIRED(53),
	
	/**
	 * Telephone authentication service is blocked due to repeated telephone authentication failures. Please try again later.
	 */
	PHONE_AUTH_BLOCKED(54),
	
	/**
	 * You have exceeded the number of times you can use the telephone authentication service in one day.
	 */
	PHONE_AUTH_EXPIRED2(55),
	
	/**
	 * The telephone authentication service is currently in progress. Please wait a moment.
	 */
	PHONE_AUTH_PROCESSING(56),
	
	/**
	 * Authorization Error ()
	 */
	AUTHORIZATION_ERROR2(57),
	
	/**
	 * Authorization Error ()
	 */
	AUTHORIZATION_ERROR3(58),
	
	/**
	 * This computer has not subscribed to the PC registration service.
	 */
	PC_REGISTRATION(59),
	
	/**
	 * Authorization Error
	 */
	AUTHORIZATION_ERROR4(60),
	
	/**
	 * An illegal program has been detected. Closing the game.
	 */
	ILLEGAL_PROGRAM(61),
	
	/**
	 * The account server is down. You cannot access it now.
	 */
	ACCOUNT_SERVER_DOWN(62);

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
