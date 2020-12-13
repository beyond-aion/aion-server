package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcAtkInstantEffect")
public class ProcAtkInstantEffect extends DamageEffect {

	@Override
	public void applyEffect(Effect effect) {
		effect.getEffected().getController().onAttack(effect, TYPE.DAMAGE, effect.getReserveds(this.position).getValue(), false, LOG.PROCATKINSTANT, hopType, false);
	}

	@Override
	protected int calculateBaseValue(Effect effect) {
		if (delta == 1 && effect.getSkillTemplate().isProvoked())
			return value;
		else
			return super.calculateBaseValue(effect);
	}
}
