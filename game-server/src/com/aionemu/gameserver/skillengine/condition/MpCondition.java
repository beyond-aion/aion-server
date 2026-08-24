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
		int cost = getCost(skill);
		if (skill.getEffector().getLifeStats().getCurrentMp() >= cost) // npcs pass the check even when they cannot afford it
			skill.getEffector().getLifeStats().reduceMp(SM_ATTACK_STATUS.TYPE.USED_MP, cost, 0, SM_ATTACK_STATUS.LOG.REGULAR);
		return true;
	}

	@Override
	public boolean canValidate(Skill skill) {
		if (!(skill.getEffector() instanceof Player player)) // npc templates carry no mp at all, so they must not be blocked by an mp cost
			return true;
		if (player.getLifeStats().getCurrentMp() >= getCost(skill))
			return true;
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_NOT_ENOUGH_MP());
		return false;
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
