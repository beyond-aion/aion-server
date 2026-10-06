package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SignetData;
import com.aionemu.gameserver.skillengine.model.SignetEnum;

/**
 * @author ATracer, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SignetBurstEffect")
public class SignetBurstEffect extends DamageEffect {

	@XmlAttribute
	protected int signetlvl;
	@XmlAttribute
	protected String signet;
	@XmlAttribute(name = "add_effect_prob_multi")
	protected int addEffectProbMultiplier = 0;
	@XmlAttribute(name = "add_effect_prob_multi_delta")
	protected float addEffectProbMultiplierDelta;

	@SuppressWarnings("lossy-conversions")
	@Override
	public void calculateDamage(Effect effect) {
		Effect signetEffect = effect.getEffected().getEffectController().getAbnormalEffect(signet);
		int valueWithDelta = calculateBaseValue(effect);

		int effectProb = 0;
		int signetLvl = Math.min(signetlvl, signetEffect == null ? 0 : signetEffect.getSkillLevel());
		SignetData signetData = DataManager.SIGNET_DATA_TEMPLATES.getSignetData(SignetEnum.valueOf(signet), signetLvl);
		if (signetData != null) {
			valueWithDelta *= signetData.getDamageMultiplier();
			effectProb = (int) ((addEffectProbMultiplier + addEffectProbMultiplierDelta * effect.getSkillLevel()) * signetData.getAddEffectProb());
		}
		effect.setSignetBurstedCount(signetLvl);
		AttackUtil.calculateSkillResult(effect, valueWithDelta, this, false);
		effect.setLaunchSubEffect(Rnd.chance() < effectProb);
		if (signetEffect != null)
			signetEffect.endEffect();
	}

	@Override
	public void calculate(Effect effect) {
		Effect signetEffect = effect.getEffected().getEffectController().getAbnormalEffect(signet);
		if (!super.calculate(effect, null, null)) {
			if (signetEffect != null) {
				signetEffect.endEffect();
			}
		}
	}

	public String getSignet() {
		return signet;
	}

	@Override
	public boolean shouldUseBoostSpellAttackEffects() {
		return false;
	}
}
