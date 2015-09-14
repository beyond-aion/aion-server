package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
public class SM_ABNORMAL_EFFECT extends AionServerPacket {

	private Creature effected;
	private int effectType = 1;// 1: creature 2: effected is player
	private int abnormals;
	private Collection<Effect> filtered;
	private int slot;

	public SM_ABNORMAL_EFFECT(Creature effected, int abnormals, Collection<Effect> effects, int slot) {
		this.abnormals = abnormals;
		this.effected = effected;
		this.filtered = effects;
		this.slot = slot;

		if (effected instanceof Player)
			effectType = 2;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(effected.getObjectId());
		writeC(effectType); // unk
		writeD(0); // TODO time
		writeD(abnormals); // unk
		writeD(0); // unk
		writeC(slot); // 4.5
		writeH(filtered.size()); // effects size

		for (Effect effect : filtered) {
			switch (effectType) {
				case 2:
					writeD(effect.getEffectorId());
				case 1:
					writeH(effect.getSkillId());
					writeC(effect.getSkillLevel());
					writeC(effect.getTargetSlot());
					writeD(effect.getRemainingTime());
					break;
				default:
					writeH(effect.getSkillId());
					writeC(effect.getSkillLevel());
			}
		}
	}
}
