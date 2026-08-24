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
		if (!canValidate(skill))
			return false;
		// npcs pass the check even when they cannot afford it and then pay what they have, down to 1 hp (example: skillId 18304)
		skill.getEffector().getLifeStats().reduceHp(SM_ATTACK_STATUS.TYPE.USED_HP, getCost(skill), 0, SM_ATTACK_STATUS.LOG.REGULAR,
			skill.getEffector());
		return true;
	}

	@Override
	public boolean canValidate(Skill skill) {
		if (!(skill.getEffector() instanceof Player player)) // npcs are never blocked by an hp cost, they pay what they have, see validate()
			return true;
		if (player.getLifeStats().getCurrentHp() > getCost(skill)) // the cast may never be lethal
			return true;
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_NOT_ENOUGH_HP());
		return false;
	}

	private int getCost(Skill skill) {
		int valueWithDelta = value + delta * skill.getSkillLevel();
		if (ratio)
			valueWithDelta = (skill.getEffector().getLifeStats().getMaxHp() * valueWithDelta) / 100;
		return valueWithDelta;
	}

	public int getHpValue() {
		return value;
	}

}
