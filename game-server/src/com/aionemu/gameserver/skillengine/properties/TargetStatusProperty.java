package com.aionemu.gameserver.skillengine.properties;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TargetStatusProperty")
public class TargetStatusProperty {

	/**
	 * @param skill
	 * @param properties
	 * @return
	 */
	public static final boolean set(final Skill skill, Properties properties) {
		if (skill.getEffectedList().size() != 1)
			return false;

		List<String> targetStatus = properties.getTargetStatus();

		Creature effected = skill.getFirstTarget();
		boolean result = false;

		if (skill.getSkillTemplate().getPenaltySkillId() == 8674)// TODO find why(skill_id="3842" name="Protective Shell I"
																															// target_status="STUN STAGGER STUMBLE SPIN OPENAERIAL")
			result = true;

		for (String status : targetStatus) {
			if (effected.getEffectController().isAbnormalSet(AbnormalState.valueOf(status)))
				result = true;
		}

		return result;
	}
}
