package com.aionemu.gameserver.skillengine.effect;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.HealType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DPHealInstantEffect")
public class DPHealInstantEffect extends AbstractHealEffect {

	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, HealType.DP);
	}

	@Override
	public void applyEffect(Effect effect) {
		super.applyEffect(effect, HealType.DP);
	}

	@Override
	public int getCurrentStatValue(Effect effect) {
		return ((Player) effect.getEffected()).getCommonData().getDp();
	}

	@Override
	public int getMaxStatValue(Effect effect) {
		return ((Player) effect.getEffected()).getGameStats().getMaxDp().getCurrent();
	}
}
