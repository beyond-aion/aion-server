package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.EffectReserved;
import com.aionemu.gameserver.skillengine.model.EffectReserved.ResourceType;

/**
 * @author Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DPTransferEffect")
public class DPTransferEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		int newValue = effect.getReserveds(position).getValue();
		((Player) effect.getEffected()).getCommonData().addDp(newValue);
		((Player) effect.getEffector()).getCommonData().addDp(-newValue);
	}

	@Override
	public void calculate(Effect effect) {
		if (!super.calculate(effect, null, null))
			return;
		effect.setReserveds(new EffectReserved(position, getCurrentStatValue(effect), ResourceType.DP, true), false);
	}

	private int getCurrentStatValue(Effect effect) {
		return ((Player) effect.getEffector()).getCommonData().getDp();
	}
}
