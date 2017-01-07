package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Sippolo, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpellAtkDrainEffect")
public class SpellAtkDrainEffect extends AbstractOverTimeEffect {

	@XmlAttribute(name = "hp_percent")
	protected int hp_percent;
	@XmlAttribute(name = "mp_percent")
	protected int mp_percent;

	@Override
	public void onPeriodicAction(Effect effect) {
		int valueWithDelta = value + delta * effect.getSkillLevel();
		int critAddDmg = this.critAddDmg2 + this.critAddDmg1 * effect.getSkillLevel();
		int damage = AttackUtil.calculateMagicalOverTimeSkillResult(effect, valueWithDelta, element, this.position, true, this.critProbMod2, critAddDmg);
		effect.getEffected().getController().onAttack(effect, TYPE.REGULAR, damage, true, LOG.SPELLATKDRAIN);
		effect.getEffector().getObserveController().notifyAttackObservers(effect.getEffected());

		// Drain (heal) portion of damage inflicted
		if (hp_percent != 0) {
			effect.getEffector().getLifeStats().increaseHp(TYPE.HP, damage * hp_percent / 100, effect.getSkillId(), LOG.SPELLATKDRAIN);
		}
		if (mp_percent != 0) {
			effect.getEffector().getLifeStats().increaseMp(TYPE.MP, damage * mp_percent / 100, effect.getSkillId(), LOG.SPELLATKDRAIN);
		}
	}
}
