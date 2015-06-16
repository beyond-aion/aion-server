package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author VladimirZ, Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InvulnerableWingEffect")
public class InvulnerableWingEffect extends EffectTemplate {

	@Override
	public void calculate(Effect effect) {
		// Only for players
		if (effect.getEffected() instanceof Player)
			super.calculate(effect, null, null);
	}

	@Override
	public void applyEffect(final Effect effect) {
		effect.addToEffectedController();
		((Player) effect.getEffected()).setInvulnerableWing(true);
	}

	@Override
	public void endEffect(Effect effect) {
		((Player) effect.getEffected()).setInvulnerableWing(false);
	}
}
