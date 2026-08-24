package com.aionemu.gameserver.skillengine.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HpUseAction")
public class HpUseAction extends Action {

	@XmlAttribute(required = true)
	protected int value;
	@XmlAttribute
	protected int delta;
	@XmlAttribute
	protected boolean ratio;

	@Override
	public boolean act(Skill skill) {
		if (!canAct(skill))
			return false;
		Creature effector = skill.getEffector();
		// npcs pass the check even when they cannot afford it and then pay what they have, down to 1 hp
		effector.getLifeStats().reduceHp(SM_ATTACK_STATUS.TYPE.USED_HP, getCost(skill), 0, SM_ATTACK_STATUS.LOG.REGULAR, effector);
		return true;
	}

	@Override
	public boolean canAct(Skill skill) {
		Creature effector = skill.getEffector();
		if (!(effector instanceof Player player))
			return true;
		if (effector.getLifeStats().getCurrentHp() > getCost(skill)) // the cast may never be lethal
			return true;
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_NOT_ENOUGH_HP());
		return false;
	}

	private int getCost(Skill skill) {
		int valueWithDelta = value + delta * skill.getSkillLevel();
		if (ratio)
			valueWithDelta = (int) (valueWithDelta / 100f * skill.getEffector().getLifeStats().getMaxHp());
		return valueWithDelta;
	}

}
