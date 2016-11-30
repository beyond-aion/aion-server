package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.TransformType;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * defines tranformtype in which player can cast given skill
 * 
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FormCondition")
public class FormCondition extends Condition {

	@XmlAttribute(required = true)
	protected TransformType value;

	@Override
	public boolean validate(Skill env) {
		if (env.getEffector() instanceof Player) {
			if (env.getEffector().getTransformModel().isActive() && env.getEffector().getTransformModel().getType() == value)
				return true;
			else {
				PacketSendUtility.sendPacket((Player) env.getEffector(), SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_CAST_IN_THIS_FORM());
				return false;
			}
		} else
			return true;
	}
}
