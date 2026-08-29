package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;

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
		if (!canValidate(skill))
			return false;
		skill.getEffector().getLifeStats().reduceMp(SM_ATTACK_STATUS.TYPE.USED_MP, getCost(skill), 0, SM_ATTACK_STATUS.LOG.REGULAR);
		return true;
	}

	@Override
	public boolean canValidate(Skill skill) {
		// npcs have no mp, so they must not be blocked by an mp cost
		if (skill.getEffector() instanceof Player player && player.getLifeStats().getCurrentMp() < getCost(skill)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_NOT_ENOUGH_MP());
			return false;
		}
		return true;
	}

	private int getCost(Skill skill) {
		int valueWithDelta = value + delta * skill.getSkillLevel();
		if (ratio)
			valueWithDelta = (skill.getEffector().getLifeStats().getMaxMp() * valueWithDelta) / 100;
		int changeMpPercent = skill.getBoostSkillCost();
		if (changeMpPercent != 0) {
			// changeMpPercent is negative
			valueWithDelta = valueWithDelta - ((valueWithDelta / ((100 / changeMpPercent))));
		}
		return valueWithDelta;
	}
}
