package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz
 */
public class SM_TRANSFORM_IN_SUMMON extends AionServerPacket {

	private Player player;
	private int summonObject;

	public SM_TRANSFORM_IN_SUMMON(Player player, Creature creature) {
		this(player, creature.getObjectId());
	}

	public SM_TRANSFORM_IN_SUMMON(Player player, int creatureObjectId) {
		this.player = player;
		this.summonObject = creatureObjectId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(summonObject);
		writeS(player.getName());
		writeD(player.getObjectId());
	}
}
