package com.aionemu.loginserver.network.aion;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains possible response that LoginServer may send to client.
 * 
 * @author KID, Neon
 */
public enum AionAuthResponse {
	/**
	 * Everything is OK.
	 */
	STR_L2AUTH_S_ALL_OK(0),

	/**
	 * System error.
	 */
	STR_L2AUTH_S_DATABASE_FAIL(1),

	/**
	 * ID or Password does not match
	 */
	STR_L2AUTH_S_INVALID_ACCOUT(2),

	/**
	 * ID or Password does not match
	 */
	STR_L2AUTH_S_INCORRECT_PWD(3),

	/**
	 * Failed to load your account info.
	 */
	STR_L2AUTH_S_ACCOUNT_LOAD_FAIL(4),

	/**
	 * Failed to load your social security number.
	 */
	STR_L2AUTH_S_LOAD_SSN_ERROR(5),

	/**
	 * No game server is available to the authorization server.
	 */
	STR_L2AUTH_S_NO_SERVER_LIST(6),

	/**
	 * You are already logged in.
	 */
	STR_L2AUTH_S_ALREADY_LOGIN(7),

	/**
	 * The selected server is down and you cannot access it.
	 */
	STR_L2AUTH_S_SERVER_DOWN(8),

	/**
	 * The login information does not match the information you provided.
	 */
	STR_L2AUTH_S_INCORRECT_MD5Key(9),

	/**
	 * No login info available.
	 */
	STR_L2AUTH_S_NO_LOGININFO(10),

	/**
	 * The connection to the server has been interrupted.
	 */
	STR_L2AUTH_S_KICKED_BY_WEB(11),

	/**
	 * You are not old enough to play the game.
	 */
	STR_L2AUTH_S_UNDER_AGE(12),

	/**
	 * Double login attempts have been detected.
	 */
	STR_L2AUTH_S_KICKED_DOUBLE_LOGIN(13),

	/**
	 * You are already logged in.
	 */
	STR_L2AUTH_S_ALREADY_PLAY_GAME(14),

	/**
	 * Sorry, the queue is full. Please try another server.
	 */
	STR_L2AUTH_S_LIMIT_EXCEED(15),

	/**
	 * The server is currently unavailable. Please try again later.
	 */
	STR_L2AUTH_S_SEVER_CHECK(16),

	/**
	 * Please login to the game after you have changed your password.
	 */
	STR_L2AUTH_S_MODIFY_PASSWORD(17),

	/**
	 * The usage period has expired, or there are temporary connection problems. You have been disconnected. Please contact customer support.
	 */
	STR_L2AUTH_S_NOT_PAID(18),

	/**
	 * You have used up your allocated time and there is no time left on this account.
	 */
	STR_L2AUTH_S_NO_SPECIFICTIME(19),

	/**
	 * System error.
	 */
	STR_L2AUTH_S_SYSTEM_ERROR(20),

	/**
	 * The IP is already in use.
	 */
	STR_L2AUTH_S_ALREADY_USED_IP(21),

	/**
	 * You cannot access the game through this IP.
	 */
	STR_L2AUTH_S_BLOCKED_IP(22),

	/**
	 * Deleted the character.
	 */
	STR_L2AUTH_S_DELETE_CHARACTER_OK(23),

	/**
	 * Created the character.
	 */
	STR_L2AUTH_S_CREATE_CHARACTER_OK(24),

	/**
	 * Invalid character name
	 */
	STR_L2AUTH_S_INVALID_NAME(25),

	/**
	 * Invalid character info
	 */
	STR_L2AUTH_S_INVALID_GENDER(26),

	/**
	 * Invalid character info (class)
	 */
	STR_L2AUTH_S_INVALID_CLASS(27),

	/**
	 * Invalid character attribute
	 */
	STR_L2AUTH_S_INVALID_ATTR(28),

	/**
	 * Exceeds the maximum number of characters
	 */
	STR_L2AUTH_S_MAX_CHAR_NUM_OVER(29),

	/**
	 * Used up all relax server time
	 */
	STR_L2AUTH_S_TIME_SERVER_LIMIT_EXCEED(30),

	/**
	 * Failed to authenticate the Security Card.
	 */
	STR_L2AUTH_S_INVALID_SECURITY_CARD(31),

	/**
	 * Age limit applied after a specified time (currently only for Thailand)
	 */
	STR_L2AUTH_S_UNDER_AGE_TIME_LIMIT(32),

	/**
	 * Restricted server.
	 */
	STR_L2AUTH_S_RESTRICTED_SERVER(33),

	/**
	 * Usage time has expired
	 */
	STR_L2AUTH_S_TIME_EXHAUSTED(34),

	/**
	 * You cannot run 2 clients in one computer in the internet cafe.
	 */
	STR_L2AUTH_S_DISALLOWED_MULTI_LOADING(35),

	/**
	 * Dormant account
	 */
	STR_L2AUTH_S_DORMANT_USER(36),

	/**
	 * Your account has not been verified.\nPlease identify yourself on the Aion Free-to-Play website (www.aionfreetoplay.com) and start again.
	 */
	STR_L2AUTH_S_AGREE_GAME(37),

	/**
	 * Waiting for parent approval.
	 */
	STR_L2AUTH_S_WAITING_PARENTS_APPROVAL(38),

	/**
	 * Waiting to leave
	 */
	STR_L2AUTH_S_WAITING_SECEDE(39),

	/**
	 * Changed Account
	 */
	STR_L2AUTH_S_LINKED_GAME_ACCOUNT(40),

	/**
	 * Request Quiz Change
	 */
	STR_L2AUTH_S_MODIFY_QUIZ(41),

	/**
	 * Exceeded the maximum number of simultaneously connected accounts for a user.
	 */
	STR_L2AUTH_S_EXCEED_CONCURRENT_PLAY_LIMIT(42),

	/**
	 * If you use an external account, you are blocked in the external authorization system.
	 */
	STR_L2AUTH_S_EXTERNAL_AUTH_BLOCKED(43),

	/**
	 * Authorization Error (%0)
	 */
	STR_L2AUTH_UNKNOWN(44),

	/**
	 * You can only run Aion after logging in on the official homepage.
	 */
	STR_L2AUTH_S_APP_LAUNCH_NOT_SUPPORTED(45),

	/**
	 * The authentication number is incorrect. Please check the number once again.
	 */
	STR_L2AUTH_NCTASD_AUTHFAILED(46),

	/**
	 * Telephone authentication service is currently under maintenance. Please try again later.
	 */
	STR_L2AUTH_NCTASD_IVRNOTAVAILABLE(47),

	/**
	 * The input time for the ARS number has expired.
	 */
	STR_L2AUTH_NCTASD_AUTHTIMEOUT(48),

	/**
	 * The telephone authentication service is unavailable as there is no phone connection or the line is busy. Please try again after moving to where
	 * there is phone connection or hanging up the phone.
	 */
	STR_L2AUTH_NCTASD_PHONENOTAVAILABLE(49),

	/**
	 * The telephone number is incorrect. Please check your registration information.
	 */
	STR_L2AUTH_NCTASD_INVALIDCUSTOMERINFO(50),

	/**
	 * Telephone authentication service is currently under maintenance. Please try again later.
	 */
	STR_L2AUTH_NCTASD_NOTAVAILABLE(51),

	/**
	 * The telephone authentication service is currently unavailable due to a high customer demand. Please try again later.
	 */
	STR_L2AUTH_NCTASD_TOOBUSY2(52),

	/**
	 * Telephone authentication service period has expired. Please try again after you have paid for the service.
	 */
	STR_L2AUTH_NCTASD_NOTPAID(53),

	/**
	 * Telephone authentication service is blocked due to repeated telephone authentication failures. Please try again later.
	 */
	STR_L2AUTH_NCTASD_AUTHBLOCKEDTEMP(54),

	/**
	 * You have exceeded the number of times you can use the telephone authentication service in one day.
	 */
	STR_L2AUTH_NCTASD_CALLLIMITEXCEEDED(55),

	/**
	 * The telephone authentication service is currently in progress. Please wait a moment.
	 */
	STR_L2AUTH_NCTASD_AUTHALREADYINPROCESS(56),

	/**
	 * Authorization Error (%0)
	 */
	STR_L2AUTH_UNKNOWN2(57),

	/**
	 * Authorization Error (%0)
	 */
	STR_L2AUTH_UNKNOWN3(58),

	/**
	 * This computer has not subscribed to the PC registration service.
	 */
	STR_L2AUTH_PCINFO_INVALID_REGISTER_PC(59),

	/**
	 * Authorization Error
	 */
	STR_L2AUTH_UNKNOWN4(60),

	/**
	 * An illegal program has been detected. Closing the game.
	 */
	STR_KICK_BOT_AUTO(61),

	/**
	 * The account server is down. You cannot access it now.
	 */
	STR_L2AUTH_S_ACCOUNTCACHESERVER_DOWN(62);

	private final int id;

	private static final Map<Integer, AionAuthResponse> responseById = new HashMap<>();

	static {
		for (AionAuthResponse val : values())
			responseById.put(val.getId(), val);
	}

	private AionAuthResponse(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static AionAuthResponse getByIdOrDefault(int id, AionAuthResponse defaultValue) {
		return responseById.getOrDefault(id, defaultValue);
	}
}
