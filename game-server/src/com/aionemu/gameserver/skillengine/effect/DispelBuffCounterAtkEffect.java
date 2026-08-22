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
	protected void resolveMagicalCritical(Effect effect) {
		// this effect type deals its damage without the magical attack calculation, so it never crits and never decides the critical of other effects
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
	public boolean shouldApplyAttackerMovementModifier() {
		return false;
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().resetDesignatedDispelEffect(effect);
		super.endEffect(effect);
	}

	@Override
	public boolean shouldUseKnowledge() {
		return false;
	}

	@Override
	public boolean shouldUseBoostSpellAttackEffects() {
		return false;
	}

	@Override
	public boolean shouldUseOneTimeBoostSkillAttack() {
		return false;
	}
}
