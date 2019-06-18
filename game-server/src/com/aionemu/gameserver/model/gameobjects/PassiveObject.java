package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.model.house.HouseRegistry;
import com.aionemu.gameserver.model.templates.housing.HousingPassiveItem;

/**
 * @author Rolandas
 */
public class PassiveObject extends HouseObject<HousingPassiveItem> {

	public PassiveObject(HouseRegistry registry, int objId, int templateId) {
		super(registry, objId, templateId);
	}

}
