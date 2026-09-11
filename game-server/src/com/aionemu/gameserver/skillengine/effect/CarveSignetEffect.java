package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer, SVDNESS
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CarveSignetEffect")
public class CarveSignetEffect extends DamageEffect {
	@XmlAttribute(name = "signet_increment", required = true)
	protected int signetIncrement = 1;
	@XmlAttribute(name = "signet_cap", required = true)
	protected int signetCap;
	@XmlAttribute(name = "signet_id", required = true)
	protected int signetId;
	@XmlAttribute(required = true)
	protected String signet;
	@XmlAttribute(required = true)
	protected int prob = 100;

	@Override
	public void calculate(Effect effect) {
		if (!super.calculate(effect, null, null)) {
			return;
		}
		Effect activeSignet = effect.getEffected().getEffectController().getAbnormalEffect(signet);
		int currentLevel = activeSignet == null ? 0 : activeSignet.getSkillLevel();
		int nextSignetLevel = currentLevel;
		// Retail: no roll is performed at cap; the signet is simply refreshed.
		if (currentLevel < signetCap && Rnd.chance() < prob) {
			nextSignetLevel = Math.min(currentLevel + signetIncrement, signetCap);
		}
		// Retail: the client receives the new signet level, while a simple refresh sends zero.
		effect.setCarvedSignet(nextSignetLevel == currentLevel ? 0 : nextSignetLevel);
	}

	@Override
	public void applyEffect(Effect effect) {
		super.applyEffect(effect);
		Effect activeSignet = effect.getEffected().getEffectController().getAbnormalEffect(signet);
		int currentLevel = activeSignet == null ? 0 : activeSignet.getSkillLevel();
		int nextSignetLevel = Math.max(currentLevel, effect.getCarvedSignet());
		if (nextSignetLevel == 0) {
			return;
		}
		if (activeSignet != null) {
			activeSignet.endEffect();
		}
		SkillEngine.getInstance().applyEffect(signetId + nextSignetLevel - 1, effect.getEffector(), effect.getEffected());
	}
}