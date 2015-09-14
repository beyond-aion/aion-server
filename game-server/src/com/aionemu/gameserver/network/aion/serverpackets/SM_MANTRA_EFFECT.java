package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sweetkr
 */
public class SM_MANTRA_EFFECT extends AionServerPacket {

	private Player player;
	private int subEffectId;

	public SM_MANTRA_EFFECT(Player player, int subEffectId) {
		this.player = player;
		this.subEffectId = subEffectId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeD(0x00);// unk
		writeD(player.getObjectId());
		writeH(subEffectId);
	}
}
