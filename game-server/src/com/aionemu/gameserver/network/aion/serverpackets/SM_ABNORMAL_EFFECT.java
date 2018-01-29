package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTargetSlot;

/**
 * @author ATracer
 */
public class SM_ABNORMAL_EFFECT extends AionServerPacket {

	private Creature effected;
	private int effectType;
	private int abnormals;
	private Collection<Effect> filtered;
	private int slots;

	public SM_ABNORMAL_EFFECT(Creature effected) {
		this(effected, effected.getEffectController().getAbnormals(), effected.getEffectController().getAbnormalEffects(), SkillTargetSlot.FULLSLOTS);
	}

	public SM_ABNORMAL_EFFECT(Creature effected, int abnormals, Collection<Effect> effects, int slots) {
		this.effected = effected;
		this.abnormals = abnormals;
		this.filtered = slots == SkillTargetSlot.FULLSLOTS ? effects
			: effects.stream().filter(e -> (slots & e.getTargetSlot().getId()) != 0).collect(Collectors.toList());
		this.slots = slots;
		this.effectType = effected instanceof Player ? 2 : 1;
	}

	@Override
	@SuppressWarnings("fallthrough")
	protected void writeImpl(AionConnection con) {
		writeD(effected.getObjectId());
		writeC(effectType); // unk
		writeD(0); // TODO time
		writeD(abnormals); // unk
		writeD(0); // unk
		writeC(slots); // 4.5
		writeH(filtered.size()); // effects size
		for (Effect effect : filtered) {
			switch (effectType) {
				case 2:
					writeD(effect.getEffectorId()); // fall-through on purpose
				case 1:
					writeH(effect.getSkillId());
					writeC(effect.getSkillLevel());
					writeC(effect.getTargetSlot().ordinal());
					writeD(effect.getRemainingTimeToDisplay());
					break;
				default:
					writeH(effect.getSkillId());
					writeC(effect.getSkillLevel());
			}
		}
	}
}
