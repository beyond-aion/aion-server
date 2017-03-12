package com.aionemu.gameserver.skillengine.effect;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatWeaponMasteryFunction;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.item.enums.ItemSubType;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WeaponMasteryEffect")
public class WeaponMasteryEffect extends BufEffect {

	@XmlAttribute(name = "weapon")
	private ItemGroup itemGroup;

	@Override
	public void startEffect(Effect effect) {
		if (change == null)
			return;

		List<IStatFunction> modifiers = getModifiers(effect);
		List<IStatFunction> masteryModifiers = new ArrayList<>();
		for (IStatFunction modifier : modifiers) {
			if (itemGroup.getItemSubType() == ItemSubType.TWO_HAND) {
				masteryModifiers.add(new StatWeaponMasteryFunction(itemGroup, modifier.getName(), modifier.getValue(), modifier.isBonus()));
			} else if (modifier.getName() == StatEnum.PHYSICAL_ATTACK || modifier.getName() == StatEnum.MAGICAL_ATTACK) {
				masteryModifiers.add(new StatWeaponMasteryFunction(itemGroup, StatEnum.MAIN_HAND_POWER, modifier.getValue(), modifier.isBonus()));
				masteryModifiers.add(new StatWeaponMasteryFunction(itemGroup, StatEnum.OFF_HAND_POWER, modifier.getValue(), modifier.isBonus()));
			}
		}
		effect.getEffected().getGameStats().addEffect(effect, masteryModifiers);
	}

}
