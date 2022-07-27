package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FallEffect")
public class FallEffect extends EffectTemplate {

	@Override
	public void calculate(Effect effect) {
		// Affects only players (for now as we dont have flying Npc's)
		if (effect.getEffected() instanceof Player)
			super.calculate(effect, null, null);
	}

	@Override
	public void applyEffect(Effect effect) {
		if (!effect.getEffected().getEffectController().isInAnyAbnormalState(AbnormalState.INVULNERABLE_WING))
			((Player) effect.getEffected()).getFlyController().endFly(true);
	}
}
