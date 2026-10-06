package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillLauncherEffect")
public class SkillLauncherEffect extends EffectTemplate {

	@XmlAttribute(name = "skill_id")
	protected int skillId;

	@Override
	public void applyEffect(Effect effect) {
		SkillEngine.getInstance().applyEffect(skillId, effect.getEffector(), effect.getEffected());
	}

	@Override
	public void calculate(Effect effect) {
		effect.addSuccessEffect(this);
	}
}
