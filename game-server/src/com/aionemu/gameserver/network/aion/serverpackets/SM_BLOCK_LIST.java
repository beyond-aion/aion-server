package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.BlockedPlayer;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Packet responsible for telling a player his block list
 * 
 * @author Ben
 */
public class SM_BLOCK_LIST extends AionServerPacket {

	@Override
	protected void writeImpl(AionConnection con) {
		// the count must match the entries that follow, while the list may change concurrently
		List<BlockedPlayer> blockedPlayers = new ArrayList<>();
		con.getActivePlayer().getBlockList().forEach(blockedPlayers::add);
		writeH(-blockedPlayers.size());
		writeC(0); // Unk
		for (BlockedPlayer player : blockedPlayers) {
			writeS(player.getName());
			writeS(player.getReason());
		}
	}
}
