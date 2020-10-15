package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.model.Effect;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DispelBuffCounterAtkEffect")
public class DispelBuffCounterAtkEffect extends DamageEffect {

	@XmlAttribute
	private int dpower;
	@XmlAttribute
	private int power;
	@XmlAttribute
	private int hitvalue;
	@XmlAttribute
	private int hitdelta;
	@XmlAttribute(name = "dispel_level")
	private int dispelLevel;

	@Override
	public int getCritProbMod2() {
		return 0; // critProbMod2 is 100 by default but this effect type cannot crit
	}

	@Override
	public void applyEffect(Effect effect) {
		super.applyEffect(effect);
		effect.getEffected().getEffectController().dispelBuffCounterAtkEffect(effect);
	}

	@Override
	public void calculateDamage(Effect effect) {
		Creature effected = effect.getEffected();
		int count = calculateBaseValue(effect);
		int finalPower = power + dpower * effect.getSkillLevel();

		int dispelledEffectCount = effected.getEffectController().calculateBuffsOrEffectorDebuffsToRemove(effect, count, dispelLevel, finalPower);
		int valueWithDelta = dispelledEffectCount > 0 ? hitvalue + ((hitvalue / 2) * (dispelledEffectCount - 1)) + hitdelta * effect.getSkillLevel() : 0;
		AttackUtil.calculateSkillResult(effect, valueWithDelta, this, false);
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().resetDesignatedDispelEffect(effect);
		super.endEffect(effect);
	}
}
