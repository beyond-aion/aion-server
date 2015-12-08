package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
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
		int maxMP = effect.getEffected().getLifeStats().getMaxMp();
		int newValue = value;
		// Support for values in percentage
		if (percent)
			newValue = (maxMP * value) / 100;
		// sm_attack_status for type and log - 4.5 checked
		effect.getEffected().getLifeStats().reduceMp(SM_ATTACK_STATUS.TYPE.DAMAGE_MP, newValue, effect.getSkillId(), SM_ATTACK_STATUS.LOG.MPATTACK);
	}
}
