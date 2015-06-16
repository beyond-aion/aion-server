package com.aionemu.gameserver.skillengine.effect.modifier;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Effect;


/**
 * @author kecimis
 *
 */
public class AbnormalDamageModifier extends ActionModifier {

	@XmlAttribute(required = true)
	protected AbnormalState state;
	/* (non-Javadoc)
	 * @see com.aionemu.gameserver.skillengine.effect.modifier.ActionModifier#analyze(com.aionemu.gameserver.skillengine.model.Effect)
	 */
	@Override
	public int analyze(Effect effect) {
		return (value + effect.getSkillLevel() * delta);
	}

	/* (non-Javadoc)
	 * @see com.aionemu.gameserver.skillengine.effect.modifier.ActionModifier#check(com.aionemu.gameserver.skillengine.model.Effect)
	 */
	@Override
	public boolean check(Effect effect) {
		return effect.getEffected().getEffectController().isAbnormalSet(state);
	}

}
