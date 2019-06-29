package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sweetkr
 */
public class SM_MANTRA_EFFECT extends AionServerPacket {

	private Creature effector;
	private int subEffectId;

	public SM_MANTRA_EFFECT(Creature effector, int subEffectId) {
		this.effector = effector;
		this.subEffectId = subEffectId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(0x00);// unk
		writeD(effector.getObjectId());
		writeH(subEffectId);
	}
}
