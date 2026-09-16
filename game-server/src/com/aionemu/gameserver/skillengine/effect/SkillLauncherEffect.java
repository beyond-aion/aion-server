package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

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
		ThreadPoolManager.getInstance().schedule(() -> {
			if (effect.getEffector().isSpawned() && !effect.getEffector().isDead())
				SkillEngine.getInstance().applyEffect(skillId, effect.getEffector(), effect.getEffected());
		}, 50);
	}

	@Override
	public void calculate(Effect effect) {
		effect.addSuccessEffect(this);
	}
}
