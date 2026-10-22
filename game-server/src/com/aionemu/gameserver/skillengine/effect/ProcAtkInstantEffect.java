package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

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
		int damage = effect.getReserveds(position).getValue();
		effect.getEffected().getController().onAttack(effect, TYPE.DAMAGE, damage, true, LOG.PROCATKINSTANT, hopType);
	}

	@Override
	public boolean shouldApplyAttackerMovementModifier() {
		return false;
	}

	@Override
	protected int calculateBaseValue(Effect effect) {
		if (delta == 1 && effect.getSkillTemplate().isProvoked())
			return value;
		else
			return super.calculateBaseValue(effect);
	}

	@Override
	public boolean shouldUseBoostSpellAttackEffects() {
		return false;
	}
}
