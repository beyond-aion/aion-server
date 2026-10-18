package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SignetData;
import com.aionemu.gameserver.skillengine.model.SignetEnum;

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
	@XmlAttribute(name = "prob_delta")
	protected int probDelta;

	@Override
	public void calculate(Effect effect) {
		if (!super.calculate(effect, null, null))
			return;

		Effect activeSignet = effect.getEffected().getEffectController().getAbnormalEffect(signet);
		int currentLevel = activeSignet == null ? 0 : activeSignet.getSkillLevel();
		int nextSignetLevel = currentLevel;
		if (currentLevel < signetCap && Rnd.chance() < calculateCarveChance(effect, currentLevel)) // no roll at cap, the signet is just refreshed
			nextSignetLevel = Math.min(currentLevel + signetIncrement, signetCap);
		// the client gets the new signet level, a plain refresh sends 0
		effect.setCarvedSignet(nextSignetLevel == currentLevel ? 0 : nextSignetLevel);
	}

	private int calculateCarveChance(Effect effect, int currentLevel) {
		SignetData signetData = DataManager.SIGNET_DATA_TEMPLATES.getSignetData(SignetEnum.valueOf(signet), currentLevel + 1);
		return (int) ((prob + probDelta * effect.getSkillLevel()) / 100f * (signetData == null ? 100 : signetData.getCarveProb()));
	}

	@Override
	public void applyEffect(Effect effect) {
		super.applyEffect(effect);

		Effect activeSignet = effect.getEffected().getEffectController().getAbnormalEffect(signet);
		int currentLevel = activeSignet == null ? 0 : activeSignet.getSkillLevel();
		int nextSignetLevel = Math.max(currentLevel, effect.getCarvedSignet());
		if (nextSignetLevel == 0)
			return;
		if (activeSignet != null)
			activeSignet.endEffect();
		SkillEngine.getInstance().applyEffect(signetId + nextSignetLevel - 1, effect.getEffector(), effect.getEffected());
	}
}
