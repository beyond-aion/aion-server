package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.network.aion.AionConnection;

/**
 * This packet is response for CM_CREATE_CHARACTER
 * 
 * @author Nemesiss, AEJTester, Neon
 */
public class SM_CREATE_CHARACTER extends AbstractPlayerInfoPacket {

	/** If response is ok */
	public static final int RESPONSE_OK = 0x00;
	/** Failed to create the character */
	public static final int FAILED_TO_CREATE_THE_CHARACTER = 1;
	/** Failed to create the character due to world db error */
	public static final int RESPONSE_DB_ERROR = 2;
	/** The number of characters exceeds the maximum allowed for the server */
	public static final int RESPONSE_SERVER_LIMIT_EXCEEDED = 4;
	/** Invalid character name */
	public static final int RESPONSE_INVALID_NAME = 5;
	/** The name includes forbidden words */
	public static final int RESPONSE_FORBIDDEN_CHAR_NAME = 9;
	/** A character with that name already exists */
	public static final int RESPONSE_NAME_ALREADY_USED = 10;
	/** The name is already reserved */
	public static final int RESPONSE_NAME_RESERVED = 11;
	/** You cannot create characters of other races in the same server */
	public static final int RESPONSE_OTHER_RACE = 12;
	/** You cannot create the selected class in the current server */
	public static final int RESPONSE_FORBIDDEN_CLASS = 20;
	/** open create characters window */
	public static final int RESPONSE_OPEN_CREATION_WINDOW = 22;

	/**
	 * response code
	 */
	private final int responseCode;
	/**
	 * Newly created player.
	 */
	private final PlayerAccountData playerAccData;

	/**
	 * Constructs new <tt>SM_CREATE_CHARACTER</tt> packet
	 * 
	 * @param accPlData
	 *          playerAccountData of player that was created
	 * @param responseCode
	 *          response code (invalid nickname, nickname is already taken, ok)
	 */

	public SM_CREATE_CHARACTER(PlayerAccountData accPlData, int responseCode) {
		this.playerAccData = accPlData;
		this.responseCode = responseCode;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(responseCode);

		if (responseCode != RESPONSE_OK)
			return;

		writePlayerInfo(playerAccData);
	}
}
