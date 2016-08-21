package com.aionemu.gameserver.skillengine.effect;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatDualWeaponMasteryFunction;
import com.aionemu.gameserver.skillengine.model.Effect;

import javolution.util.FastTable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WeaponDualEffect")
public class WeaponDualEffect extends BufEffect {

	@Override
	public void startEffect(Effect effect) {
		if (change == null)
			return;

		if (effect.getEffected() instanceof Player)
			((Player) effect.getEffected()).setDualEffectValue(value);

		List<IStatFunction> modifiers = getModifiers(effect);
		List<IStatFunction> masteryModifiers = new FastTable<>();
		for (IStatFunction modifier : modifiers) {
			masteryModifiers.add(new StatDualWeaponMasteryFunction(effect, modifier));
		}
		if (masteryModifiers.size() > 0) {
			effect.getEffected().getGameStats().addEffect(effect, masteryModifiers);
		}
	}

	@Override
	public void endEffect(Effect effect) {
		if (effect.getEffected() instanceof Player)
			((Player) effect.getEffected()).setDualEffectValue(0);

		super.endEffect(effect);
	}

}
