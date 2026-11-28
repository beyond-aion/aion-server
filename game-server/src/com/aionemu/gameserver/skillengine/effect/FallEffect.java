package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FallEffect")
public class FallEffect extends EffectTemplate {

	@Override
	protected boolean isDodgedOrResisted(Effect effect, StatEnum statEnum) {
		return hasInvulnerableWings(effect.getEffected()) || super.isDodgedOrResisted(effect, statEnum);
	}

	@Override
	public void applyEffect(Effect effect) {
		Player player = getPlayerOrSummonMaster(effect.getEffected());
		if (player != null)
			player.getFlyController().forceEndFlight();
	}
}
