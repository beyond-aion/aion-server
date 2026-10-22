package com.aionemu.gameserver.skillengine.condition;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NoFlyingCondition")
public class NoFlyingCondition extends Condition {

	@Override
	public boolean validate(Skill env) {
		return (!env.getEffector().isFlying());
	}

	@Override
	public boolean validate(Effect effect) {
		return (!effect.getEffected().isFlying());
	}

}
