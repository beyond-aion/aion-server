package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.EffectReserved;
import com.aionemu.gameserver.skillengine.model.EffectReserved.ResourceType;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MpAttackInstantEffect")
public class MpAttackInstantEffect extends EffectTemplate {

	@XmlAttribute
	protected boolean percent;

	@Override
	public void calculate(Effect effect) {
		int maxMP = effect.getEffected().getLifeStats().getMaxMp();
		int newValue = value;
		// Support for values in percentage
		if (percent)
			newValue = ((maxMP * value) / 100);

		effect.setReserveds(new EffectReserved(position, newValue, ResourceType.MP, true), false);

		this.calculate(effect, null, null, element);
	}

	@Override
	public void applyEffect(Effect effect) {
		effect.getEffected().getLifeStats().reduceMp(SM_ATTACK_STATUS.TYPE.DAMAGE_MP, effect.getReserveds(position).getValue(), effect.getSkillId(), SM_ATTACK_STATUS.LOG.MPATTACK);
	}
}
