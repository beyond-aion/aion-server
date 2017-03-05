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
		// TODO find out why skill 2504-2506 ("Protective Shell") has target_status="STUN STAGGER STUMBLE SPIN OPENAERIAL"
		if (skill.getSkillTemplate().getStack().equals("RI_PROTECTIONCURTAIN"))
			return true;

		skill.getEffectedList().removeIf(effected -> !hasAnyAbnormalState(effected, properties.getTargetStatus()));

		// if first target was filtered out (= he had no required abnormal state), the skill cannot be cast
		return skill.getEffectedList().contains(skill.getFirstTarget());
	}

	private static boolean hasAnyAbnormalState(Creature creature, List<AbnormalState> states) {
		return states.stream().anyMatch(state -> creature.getEffectController().isAbnormalSet(state));
	}
}
