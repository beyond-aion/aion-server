package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAttribute;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author kecimis, Cheatkiller
 */
public class DelayedSkillEffect extends EffectTemplate {

	@XmlAttribute(name = "skill_id")
	protected int skillId;

	@Override
	public void applyEffect(final Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void endEffect(Effect effect) {
		super.endEffect(effect);
		if (effect.isEndedByTime() && !effect.getEffected().isDead()) {
			SkillTemplate st = DataManager.SKILL_DATA.getSkillTemplate(skillId);
			SkillEngine.getInstance().getSkill(effect.getEffector(), skillId, st.getLvl(), effect.getEffected()).useWithoutPropSkill();
		}
	}
}
