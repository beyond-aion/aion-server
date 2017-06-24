package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RESURRECT;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResurrectEffect")
public class ResurrectEffect extends EffectTemplate {

	@XmlAttribute(name = "skill_id")
	protected int skillId;

	@Override
	public void applyEffect(Effect effect) {
		if (effect.getEffected() instanceof Player) {
			Player effectedPlayer = (Player) effect.getEffected();
			effectedPlayer.setPlayerResActivate(true);
			effectedPlayer.setResurrectionSkill(skillId);
			PacketSendUtility.sendPacket(effectedPlayer, new SM_RESURRECT(effect.getEffector(), effect.getSkillId()));
		}
	}

	@Override
	public void calculate(Effect effect) {
		if (effect.getEffected() instanceof Player && effect.getEffected().isDead())
			super.calculate(effect, null, null);
	}
}
