package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MpAttackEffect")
public class MpAttackEffect extends AbstractOverTimeEffect {

	// TODO bosses are resistent to this?

	@Override
	public void onPeriodicAction(Effect effect) {
		int baseValue = calculateBaseValue(effect);
		int amount = percent ? effect.getEffected().getLifeStats().getMaxMp() * baseValue / 100 : baseValue;
		effect.getEffected().getLifeStats().reduceMp(TYPE.DAMAGE_MP, amount, effect.getSkillId(), LOG.MPATTACK);
	}
}
