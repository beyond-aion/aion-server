package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

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
		// Only players have FP
		if (effect.getEffected() instanceof Player)
			super.calculate(effect, null, null);
	}

	@Override
	public void onPeriodicAction(Effect effect) {
		Player effected = (Player) effect.getEffected();
		int maxFP = effected.getLifeStats().getMaxFp();
		int newValue = value;
		// Support for values in percentage
		if (percent)
			newValue = (maxFP * value) / 100;
		effected.getLifeStats().reduceFp(TYPE.FP_DAMAGE, newValue, effect.getSkillId(), LOG.FPATTACK);
	}
}
