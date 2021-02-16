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
	private int hpPercent;
	@XmlAttribute(name = "mp_percent")
	private int mpPercent;

	@Override
	public void onPeriodicAction(Effect effect) {
		int valueWithDelta = calculateBaseValue(effect);
		int critAddDmg = critAddDmg2 + critAddDmg1 * effect.getSkillLevel();
		int damage = AttackUtil.calculateMagicalOverTimeSkillResult(effect, valueWithDelta, element, position, true, critProbMod2, critAddDmg);
		effect.getEffected().getController().onAttack(effect, TYPE.DAMAGE, damage, true, LOG.SPELLATKDRAIN, hopType);
		effect.getEffector().getObserveController().notifyAttackObservers(effect.getEffected(), effect.getSkillId());

		// Drain (heal) portion of damage inflicted
		if (hpPercent != 0) {
			effect.getEffector().getLifeStats().increaseHp(TYPE.HP, damage * hpPercent / 100, effect, LOG.SPELLATKDRAIN);
		}
		if (mpPercent != 0) {
			effect.getEffector().getLifeStats().increaseMp(TYPE.MP, damage * mpPercent / 100, effect.getSkillId(), LOG.SPELLATKDRAIN);
		}
	}
}
