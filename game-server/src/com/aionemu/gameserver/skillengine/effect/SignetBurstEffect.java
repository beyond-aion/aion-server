package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.attack.AttackUtil;
import com.aionemu.gameserver.skillengine.model.Effect;

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

	@Override
	public void calculateDamage(Effect effect) {
		Effect signetEffect = effect.getEffected().getEffectController().getAnormalEffect(signet);
		int valueWithDelta = value + delta * effect.getSkillLevel();

		if (signetEffect == null) {
			valueWithDelta *= 0.05f;
			AttackUtil.calculateSkillResult(effect, valueWithDelta, this, false);
			effect.setLaunchSubEffect(false);
		} else {
			int level = signetEffect.getSkillLevel();
			effect.setSignetBurstedCount(level);
			int failChance = 0;
			switch (level) {
				case 1:
					valueWithDelta *= 0.2f;
					failChance = 90;
					break;
				case 2:
					valueWithDelta *= 0.5f;
					failChance = 70;
					break;
				case 3:
					valueWithDelta *= 1.0f;
					break;
				case 4:
					valueWithDelta *= 1.2f;
					break;
				case 5:
					valueWithDelta *= 1.5f;
					break;
			}
			AttackUtil.calculateSkillResult(effect, valueWithDelta, this, false);
			if (Rnd.get(0, 100) < failChance) {
				effect.setLaunchSubEffect(false);
			}
			if (signetEffect != null) {
				signetEffect.endEffect();
			}
		}
	}

	@Override
	public void calculate(Effect effect) {
		Effect signetEffect = effect.getEffected().getEffectController().getAnormalEffect(signet);
		if (!super.calculate(effect, null, null)) {
			if (signetEffect != null) {
				signetEffect.endEffect();
			}
		}
	}

	
	/**
	 * @return the signetlvl
	 */
	public int getSignetlvl() {
		return signetlvl;
	}

	
	/**
	 * @return the signet
	 */
	public String getSignet() {
		return signet;
	}
	
	
}
