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
@XmlType(name = "MpUseAction")
public class MpUseAction extends Action {

	@XmlAttribute(required = true)
	protected int value;

	@XmlAttribute
	protected int delta;

	@XmlAttribute
	protected boolean ratio;

	@Override
	public boolean act(Skill skill) {
		Creature effector = skill.getEffector();
		int currentMp = effector.getLifeStats().getCurrentMp();
		int valueWithDelta = value + delta * skill.getSkillLevel();
		if (ratio)
			valueWithDelta = skill.getEffector().getLifeStats().getMaxMp() * valueWithDelta / 100;
		int changeMpPercent = skill.getBoostSkillCost();
		if (changeMpPercent != 0) {
			// changeMpPercent is negative
			valueWithDelta = valueWithDelta - ((valueWithDelta / ((100 / changeMpPercent))));
		}

		if (effector instanceof Player) {
			if (currentMp <= 0 || currentMp < valueWithDelta) {
				PacketSendUtility.sendPacket((Player) effector, SM_SYSTEM_MESSAGE.STR_SKILL_NOT_ENOUGH_MP());
				return false;
			}
		}

		effector.getLifeStats().reduceMp(SM_ATTACK_STATUS.TYPE.USED_MP, valueWithDelta, 0, SM_ATTACK_STATUS.LOG.REGULAR);
		return true;
	}

}
