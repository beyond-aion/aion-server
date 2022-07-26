package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
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
	public void applyEffect(Effect effect) {
		super.applyEffect(effect);

		if (Rnd.chance() >= prob)
			return;

		int nextSignetLevel = signetIncrement;
		Effect activeSignet = effect.getEffected().getEffectController().getAbnormalEffect(signet);
		if (activeSignet != null) {
			activeSignet.endEffect();
			nextSignetLevel = Math.min(activeSignet.getCarvedSignet() + signetIncrement, Math.max(signetCap, activeSignet.getCarvedSignet()));
		}
		Effect signet = SkillEngine.getInstance().applyEffect(signetId + nextSignetLevel - 1, effect.getEffector(), effect.getEffected());
		signet.setCarvedSignet(nextSignetLevel);
	}
}
