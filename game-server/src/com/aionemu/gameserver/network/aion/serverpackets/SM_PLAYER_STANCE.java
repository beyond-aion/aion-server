package com.aionemu.gameserver.network.aion.serverpackets;


import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author prix
 */
public class SM_PLAYER_STANCE extends AionServerPacket {

	private Player player;
	private int state;

	public SM_PLAYER_STANCE(Player player, int state) {
		this.player = player;
		this.state = state; // 0 = off, 1 = block, flight, glide, jump, etc.
		// 2 = stationary object
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(player.getObjectId());
		writeC(state);
	}
}
