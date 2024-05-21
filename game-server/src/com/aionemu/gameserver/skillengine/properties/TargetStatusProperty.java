package com.aionemu.gameserver.skillengine.properties;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TargetStatusProperty")
public class TargetStatusProperty {

	public static boolean set(Properties properties, Properties.ValidationResult result, SkillTemplate skillTemplate) {
		// TODO find out why skill 2504-2506 ("Protective Shell") has target_status="STUN STAGGER STUMBLE SPIN OPENAERIAL"
		if (skillTemplate.getStack().equals("RI_PROTECTIONCURTAIN"))
			return true;

		result.getTargets().removeIf(effected -> !hasAnyAbnormalState(effected, properties.getTargetStatus()));

		// if first target was filtered out (= he had no required abnormal state), the skill cannot be cast
		return result.getTargets().contains(result.getFirstTarget());
	}

	private static boolean hasAnyAbnormalState(Creature creature, List<AbnormalState> states) {
		return states.stream().anyMatch(state -> creature.getEffectController().isAbnormalSet(state));
	}
}
