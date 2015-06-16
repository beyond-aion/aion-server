package com.aionemu.gameserver.skillengine.effect;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_COOLDOWN;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas, Luzien
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillCooltimeResetEffect")
public class SkillCooltimeResetEffect extends EffectTemplate {

	@XmlAttribute(name = "first_cd", required = true)
	protected int firstCd;

	@XmlAttribute(name = "last_cd", required = true)
	protected int lastCd;

	@Override
	public void applyEffect(Effect effect) {
		Creature effected = effect.getEffected();
		HashMap<Integer, Long> resetSkillCoolDowns = new HashMap<>();
		for (int i = firstCd; i <= lastCd; i++) {
			long delay = effected.getSkillCoolDown(i) - System.currentTimeMillis();
			if (delay <= 0)
				continue;
			if (delta > 0) // TODO: Percent of remaining CD or original cd?
				delay -= delay * (delta / 100);
			else
				delay -= value;

			effected.setSkillCoolDown(i, delay + System.currentTimeMillis());
			resetSkillCoolDowns.put(i, delay + System.currentTimeMillis());
		}
		if (effected instanceof Player) {
			if (resetSkillCoolDowns.size() > 0)
				PacketSendUtility.sendPacket((Player) effected, new SM_SKILL_COOLDOWN(resetSkillCoolDowns));
		}
	}

}
