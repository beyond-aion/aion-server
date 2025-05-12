package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.skillengine.model.Effect;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResurrectBaseEffect")
public class ResurrectBaseEffect extends ResurrectEffect {

	@Override
	public void calculate(Effect effect) {
		calculate(effect, null, null);
	}

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void endEffect(Effect effect) {
		Creature effected = effect.getEffected();
		Creature effector = effect.getEffector();
		if (effected.isDead() && effected instanceof Player player) {
			if (effector instanceof Player p)
				if (player.getRace() == p.getRace())
					return;
			PlayerReviveService.scheduleReviveAtBase(player, 2500, skillId);
		}
	}
}
