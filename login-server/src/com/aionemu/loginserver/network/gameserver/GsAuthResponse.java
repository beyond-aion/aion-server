package com.aionemu.loginserver.network.gameserver;

/**
 * This class contains possible response that LoginServer may send to gameserver if authentication fail etc.
 * 
 * @author -Nemesiss-
 */
public enum GsAuthResponse {
	/**
	 * Everything is OK
	 */
	AUTHED(0),
	/**
	 * Password/IP etc does not match.
	 */
	NOT_AUTHED(1),
	/**
	 * Requested id is not free
	 */
	ALREADY_REGISTERED(2);

	/**
	 * id of this enum that may be sent to client
	 */
	private byte responseId;

	/**
	 * Constructor.
	 * 
	 * @param responseId
	 *          id of the message
	 */
	private GsAuthResponse(int responseId) {
		this.responseId = (byte) responseId;
	}

	/**
	 * Message Id that may be sent to client.
	 * 
	 * @return message id
	 */
	public byte getResponseId() {
		return responseId;
	}
}
