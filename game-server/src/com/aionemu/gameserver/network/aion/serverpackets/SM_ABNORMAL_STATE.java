package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Avol, ATracer
 */
public class SM_ABNORMAL_STATE extends AionServerPacket {

	private Collection<Effect> effects;
	private int abnormals;
	private Player player;
	private int slot;

	public SM_ABNORMAL_STATE(Player player, Collection<Effect> effects, int abnormals, int slot) {
		this.player = player;
		this.effects = effects;
		this.abnormals = abnormals;
		this.slot = slot;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(abnormals);
		writeD(0);
		writeD(0);//4.5
		writeC(slot);
		writeH(effects.size());

		for (Effect effect : effects) {
			writeD(effect.getEffectorId());
			writeH(effect.getSkillId());
			writeC(effect.getSkillLevel());
			writeC(effect.getTargetSlot());
			writeD(effect.getRemainingTime());
		}
	}
}
