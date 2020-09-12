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
	protected int dpower;
	@XmlAttribute
	protected int power;
	@XmlAttribute
	protected int hitvalue;
	@XmlAttribute
	protected int hitdelta;
	@XmlAttribute(name = "dispel_level")
	protected int dispelLevel;
	private int finalPower;

	@Override
	public void applyEffect(Effect effect) {
		super.applyEffect(effect);
		effect.getEffected().getEffectController().dispelBuffCounterAtkEffect(effect);
	}

	@Override
	public void calculateDamage(Effect effect) {
		Creature effected = effect.getEffected();
		int count = calculateBaseValue(effect);
		finalPower = power + dpower * effect.getSkillLevel();

		int dispelledEffectCount = count - effected.getEffectController().calculateEffectsToRemove(effect, count, dispelLevel, finalPower);
		int newValue = 0;
		if (dispelledEffectCount == 1)
			newValue = hitvalue;
		else if (dispelledEffectCount > 1)
			newValue = hitvalue + ((hitvalue / 2) * (dispelledEffectCount - 1));

		int valueWithDelta = newValue + hitdelta * effect.getSkillLevel();

		AttackUtil.calculateSkillResult(effect, valueWithDelta, this, false);
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().resetDesignatedDispelEffect(effect);
		super.endEffect(effect);
	}
}
