package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.properties.FirstTargetAttribute;
import com.aionemu.gameserver.skillengine.properties.TargetRangeAttribute;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TargetCondition")
public class TargetCondition extends Condition {

	@XmlAttribute(required = true)
	protected TargetAttribute value;

	/**
	 * Gets the value of the value property.
	 * 
	 * @return possible object is {@link TargetAttribute }
	 */
	public TargetAttribute getValue() {
		return value;
	}

	@Override
	public boolean validate(Skill skill) {
		if (value == TargetAttribute.NONE || value == TargetAttribute.ALL)
			return true;
		if (skill.getSkillTemplate().getProperties().getTargetType().equals(TargetRangeAttribute.AREA))
			return true;
		if (skill.getSkillTemplate().getProperties().getFirstTarget() != FirstTargetAttribute.TARGET
			&& skill.getSkillTemplate().getProperties().getFirstTarget() != FirstTargetAttribute.TARGETORME)
			return true;
		if (skill.getSkillTemplate().getProperties().getFirstTarget() == FirstTargetAttribute.TARGETORME && skill.getEffector() == skill.getFirstTarget())
			return true;

		boolean result = false;
		switch (value) {
			case NPC:
				result = (skill.getFirstTarget() instanceof Npc);
				break;
			case PC:
				result = (skill.getFirstTarget() instanceof Player);
				break;
		}

		if (!result && skill.getEffector() instanceof Player)
			PacketSendUtility.sendPacket((Player) skill.getEffector(), SM_SYSTEM_MESSAGE.STR_SKILL_TARGET_IS_NOT_VALID());

		return result;
	}
}
