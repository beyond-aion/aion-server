package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
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
		// Only players have FP
		if (effect.getEffected() instanceof Player) {
			Player player = (Player) effect.getEffected();
			int maxFP = player.getLifeStats().getMaxFp();
			int newValue = value;
			// Support for values in percentage
			if (percent)
				newValue = (maxFP * value) / 100;

			effect.setReserveds(new EffectReserved(position, newValue, ResourceType.FP, true), false);

			super.calculate(effect, null, null);
		}
	}

	@Override
	public void applyEffect(Effect effect) {
		// Restriction to players because lack of FP on other Creatures
		if (!(effect.getEffected() instanceof Player))
			return;
		Player player = (Player) effect.getEffected();
		player.getLifeStats().reduceFp(TYPE.FP_DAMAGE, effect.getReserveds(position).getValue(), effect.getSkillId(), SM_ATTACK_STATUS.LOG.FPATTACK);
	}
}
