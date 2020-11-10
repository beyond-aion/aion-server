package com.aionemu.chatserver.network.gameserver;

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

	private final byte responseId;

	GsAuthResponse(int responseId) {
		this.responseId = (byte) responseId;
	}

	public byte getResponseId() {
		return responseId;
	}
}
