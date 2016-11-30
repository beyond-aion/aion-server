package com.aionemu.gameserver.skillengine.effect.modifier;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author kecimis
 */
public class AbnormalDamageModifier extends ActionModifier {

	@XmlAttribute(required = true)
	protected AbnormalState state;

	@Override
	public int analyze(Effect effect) {
		return (value + effect.getSkillLevel() * delta);
	}

	@Override
	public boolean check(Effect effect) {
		return effect.getEffected().getEffectController().isAbnormalSet(state);
	}

}
