package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MpCondition")
public class MpCondition extends Condition {

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
			valueWithDelta = (skill.getEffector().getLifeStats().getMaxMp() * valueWithDelta) / 100;
		int changeMpPercent = skill.getBoostSkillCost();
		if (changeMpPercent != 0) {
			// changeMpPercent is negative
			valueWithDelta = valueWithDelta - ((valueWithDelta / ((100 / changeMpPercent))));
		}
		if (skill.getEffector().getLifeStats().getCurrentMp() > valueWithDelta)
			skill.getEffector().getLifeStats().reduceMp(SM_ATTACK_STATUS.TYPE.USED_MP, valueWithDelta, 0, SM_ATTACK_STATUS.LOG.REGULAR);
		return skill.getEffector().getLifeStats().getCurrentMp() > valueWithDelta;
	}
}
