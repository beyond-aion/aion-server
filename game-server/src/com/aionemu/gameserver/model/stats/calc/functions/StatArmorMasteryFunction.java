package com.aionemu.gameserver.model.stats.calc.functions;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.enums.ItemSubType;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author ATracer (based on Mr.Poke ArmorMasteryModifier)
 */
public class StatArmorMasteryFunction extends StatRateFunction {

	private final ItemSubType armorType;
	private final int fixedBonus;
	private int equipmentFactor;

	public StatArmorMasteryFunction(ItemSubType armorType, StatEnum name, int value, boolean bonus, int fixedBonus, List<Item> equipment) {
		super(name, value, bonus);
		this.armorType = armorType;
		this.fixedBonus = fixedBonus;
		updateEquipmentFactor(equipment);
	}

	public void updateEquipmentFactor(List<Item> equipment) {
		equipmentFactor = 0;
		for (Item item : equipment) {
			if (item.getItemTemplate().getItemSubType() == armorType) {
				equipmentFactor += getEquipmentFactor(ItemSlot.getSlotFor(item.getEquipmentSlot()));
			}
		}
	}

	private int getEquipmentFactor(ItemSlot itemSlot) {
		return switch (itemSlot) {
			case TORSO -> 30;
			case PANTS -> 25;
			case SHOULDER, GLOVES, BOOTS -> 15;
			default -> 0;
		};
	}

	@Override
	public void apply(Stat2 stat, CalculationType... calculationTypes) {
		super.apply(stat, calculationTypes);
		if (fixedBonus != 0 && equipmentFactor != 0)
			stat.addToBonus(fixedBonus * equipmentFactor / 100f);
	}

	@Override
	public int getValue() {
		return value * equipmentFactor / 100; // truncation from equipmentFactor is retail-like
	}
}
