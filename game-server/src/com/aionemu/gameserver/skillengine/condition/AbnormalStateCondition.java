package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author kecimis
 */
public class AbnormalStateCondition extends Condition {

	@XmlAttribute(required = true)
	protected AbnormalState value;

	@Override
	public boolean validate(Skill env) {
		if (env.getFirstTarget() != null)
			return (env.getFirstTarget().getEffectController().isAbnormalSet(value));
		return false;
	}

	@Override
	public boolean validate(Effect effect) {
		if (effect.getEffected() != null)
			return (effect.getEffected().getEffectController().isAbnormalSet(value));
		return false;
	}

}
