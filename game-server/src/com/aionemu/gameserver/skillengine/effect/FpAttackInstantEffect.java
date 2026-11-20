package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.EffectReserved;
import com.aionemu.gameserver.skillengine.model.EffectReserved.ResourceType;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FpAttackInstantEffect")
public class FpAttackInstantEffect extends EffectTemplate {

	@XmlAttribute
	protected boolean percent;

	@Override
	public void calculate(Effect effect) {
		Player owner = getPlayerOrSummonMaster(effect.getEffected());
		if (owner == null || !super.calculate(effect, null, null))
			return;
		int baseValue = calculateBaseValue(effect);
		int amount = percent ? owner.getLifeStats().getMaxFp() * baseValue / 100 : baseValue;
		effect.setReserveds(new EffectReserved(position, amount, ResourceType.FP, true), false);
	}

	@Override
	public void applyEffect(Effect effect) {
		Player owner = getPlayerOrSummonMaster(effect.getEffected());
		if (owner != null)
			owner.getLifeStats().reduceFp(TYPE.FP_DAMAGE_INSTANT, effect.getReserveds(position).getValue(), effect.getSkillId(), LOG.FPATTACK);
	}
}
