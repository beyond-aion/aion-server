package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author Tomate
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HpCondition")
public class HpCondition extends Condition {

	@XmlAttribute(required = true)
	protected int value;
	@XmlAttribute
	protected int delta;
	@XmlAttribute
	protected boolean ratio;

	@Override
	public boolean validate(Skill skill) {

		int valueWithDelta = value + delta * skill.getSkillLevel();
		if (ratio)
			valueWithDelta = (skill.getEffector().getLifeStats().getMaxHp() * valueWithDelta) / 100;
		if (skill.getEffector().getLifeStats().getCurrentHp() > valueWithDelta)
			skill.getEffector().getLifeStats()
				.reduceHp(SM_ATTACK_STATUS.TYPE.USED_HP, valueWithDelta, 0, SM_ATTACK_STATUS.LOG.REGULAR, skill.getEffector());
		return skill.getEffector().getLifeStats().getCurrentHp() >= valueWithDelta;
	}

	public int getHpValue() {
		return value;
	}

}
