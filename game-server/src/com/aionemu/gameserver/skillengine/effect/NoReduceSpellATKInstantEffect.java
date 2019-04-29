package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NoReduceSpellATKInstantEffect")
public class NoReduceSpellATKInstantEffect extends DamageEffect {

	@XmlAttribute
	protected boolean percent;
	@XmlAttribute(name = "max_damage")
	protected int max_damage;

	@Override
	public void calculateDamage(Effect effect) {
		int valueWithDelta = calculateBaseValue(effect);
		if (percent) {
			float percentToCount = valueWithDelta / 100f;
			valueWithDelta = (int) (effect.getEffected().getLifeStats().getMaxHp() * percentToCount);
		}

		if (max_damage > 0)
			valueWithDelta = valueWithDelta > max_damage ? max_damage : valueWithDelta;

		AttackUtil.calculateSkillResult(effect, valueWithDelta, this, false);
	}
}
