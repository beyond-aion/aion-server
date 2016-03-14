package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author Cheatkiller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RideRobotCondition")
public class RideRobotCondition extends Condition {

	@Override
	public boolean validate(Skill skill) {
		if (skill.getEffector() instanceof Player) {
			return ((Player) skill.getEffector()).isInRobotMode();
		} else {
			return true;
		}
	}
}
