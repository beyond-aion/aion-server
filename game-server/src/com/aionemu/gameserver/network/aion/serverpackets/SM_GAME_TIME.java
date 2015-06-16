package com.aionemu.gameserver.network.aion.serverpackets;


import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.utils.gametime.GameTimeManager;

/**
 * Sends the current time in the server in minutes since 1/1/00 00:00:00
 * 
 * @author Ben
 */
public class SM_GAME_TIME extends AionServerPacket {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeD(GameTimeManager.getGameTime().getTime()); // Minutes since 1/1/00 00:00:00
	}

}
