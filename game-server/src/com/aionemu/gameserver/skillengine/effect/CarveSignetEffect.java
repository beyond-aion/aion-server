package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CarveSignetEffect")
public class CarveSignetEffect extends DamageEffect {

	@XmlAttribute(required = true)
	protected int signetlvlstart;
	@XmlAttribute(required = true)
	protected int signetlvl;
	@XmlAttribute(required = true)
	protected int signetid;
	@XmlAttribute(required = true)
	protected String signet;
	@XmlAttribute(required = true)
	protected int prob = 100;

	private int nextSignetLevel = 1;

	@Override
	public void applyEffect(Effect effect) {
		super.applyEffect(effect);

		if (Rnd.get(0, 100) > prob)
			return;
		
		Effect placedSignet = effect.getEffected().getEffectController().getAnormalEffect(signet);

		if (placedSignet != null)
			placedSignet.endEffect();

		SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(signetid + nextSignetLevel - 1);
		Effect newEffect = new Effect(effect.getEffector(), effect.getEffected(), template, nextSignetLevel, 0);
		newEffect.initialize();
		newEffect.applyEffect();
	}

	@Override
	public void calculate(Effect effect) {
		if (!super.calculate(effect, null, null))
			return;
		Effect placedSignet = effect.getEffected().getEffectController().getAnormalEffect(signet);
		nextSignetLevel = signetlvlstart > 0 ? signetlvlstart : 1;
		effect.setCarvedSignet(nextSignetLevel);
		if (placedSignet != null) {
			nextSignetLevel = placedSignet.getSkillId() - this.signetid + 2;
			if ((signetlvlstart > 0) && (nextSignetLevel < signetlvlstart))
				nextSignetLevel = signetlvlstart;

			effect.setCarvedSignet(nextSignetLevel);
			if (nextSignetLevel > signetlvl || nextSignetLevel > 5)
				nextSignetLevel--;
		}
	}
}
