package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReturnEffect")
public class ReturnEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		TeleportService.moveToBindLocation((Player) effect.getEffector());
	}

	@Override
	public void calculate(Effect effect) {
		if (effect.getEffected().isSpawned())
			effect.addSuccessEffect(this);
	}
}
