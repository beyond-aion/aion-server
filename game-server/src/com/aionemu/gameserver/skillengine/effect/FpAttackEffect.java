package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FpAttackEffect")
public class FpAttackEffect extends AbstractOverTimeEffect {

	@Override
	public void calculate(Effect effect) {
		if (getPlayerOrSummonMaster(effect.getEffected()) != null)
			super.calculate(effect, null, null);
	}

	@Override
	public void onPeriodicAction(Effect effect) {
		Player owner = getPlayerOrSummonMaster(effect.getEffected());
		if (owner == null)
			return;
		int baseValue = calculateBaseValue(effect);
		int amount = percent ? owner.getLifeStats().getMaxFp() * baseValue / 100 : baseValue;
		owner.getLifeStats().reduceFp(TYPE.FP_DAMAGE, amount, effect.getSkillId(), LOG.FPATTACK);
	}
}
