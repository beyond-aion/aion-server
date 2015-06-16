package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.ChargeSkillEntry;
import com.aionemu.gameserver.skillengine.model.ChargedSkill;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillChargeCondition")
public class SkillChargeCondition extends ChargeCondition {
	
	@Override
	public boolean validate(Skill env) {
		int castTime = 0;
		if (env.getEffector() instanceof Player) {
			ChargeSkillEntry skillCharge = DataManager.SKILL_CHARGE_DATA.getChargedSkillEntry(value);
			env.getChargeSkillList().addAll(skillCharge.getSkills());
			for (ChargedSkill skill : env.getChargeSkillList()) {
				castTime += skill.getTime();
			}
			env.setDuration(castTime);
		}
		return true;
	}
	
	public int getValue() {
		return value;
	}
}
