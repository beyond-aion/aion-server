package com.aionemu.gameserver.skillengine.effect;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatShieldMasteryFunction;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author VladimirZ
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ShieldMasteryEffect")
public class ShieldMasteryEffect extends BufEffect {

	@Override
	public void startEffect(Effect effect) {

		List<IStatFunction> modifiers = getModifiers(effect);
		List<IStatFunction> masteryModifiers = new ArrayList<>();
		for (IStatFunction modifier : modifiers) {
			masteryModifiers.add(new StatShieldMasteryFunction(modifier.getName(), modifier.getValue(), modifier.isBonus()));
		}
		if (masteryModifiers.size() > 0) {
			effect.getEffected().getGameStats().addEffect(effect, masteryModifiers);
		}
	}
}
