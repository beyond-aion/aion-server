package com.aionemu.gameserver.model.templates.pet;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author SVDNESS
 * @version 4.8 [JDK 25]
 */

@XmlType(name="FoodType")
@XmlEnum
public enum FoodType {
	AETHER_CHERRY,
	AETHER_CRYSTAL_BISCUIT,
	AETHER_GEM_BISCUIT,
	AETHER_POWDER_BISCUIT,
	ARMOR,
	BALAUR_SCALES,
	BONES,
	EXCLUDES,
	FLUIDS,
	HEALTHY_FOOD_ALL,
	HEALTHY_FOOD_SPICY,
	MISCELLANEOUS,
	POPPY_SNACK,
	POPPY_SNACK_TASTY,
	POPPY_SNACK_NUTRITIOUS,
	SOULS,
	SHUGO_EVENT_COIN,
	STINKY,
	THORNS,
	NEW_YEAR_PET_FOOD;

	public String value() {
		return name();
	}

	public static FoodType fromValue(String value) {
		return valueOf(value);
	}
}