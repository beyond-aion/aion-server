package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.network.aion.AionConnection;

/**
 * @author Avol, ATracer, Neon
 */
public class SM_UPDATE_PLAYER_APPEARANCE extends AbstractPlayerInfoPacket {

	private int playerId;
	private List<Item> items;

	public SM_UPDATE_PLAYER_APPEARANCE(int playerId, List<Item> items) {
		this.playerId = playerId;
		this.items = items;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerId);
		writeEquippedItems(items);
	}
}
