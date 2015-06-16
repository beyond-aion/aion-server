package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HousingPassiveItem;

/**
 * @author Rolandas
 */
public class PassiveObject extends HouseObject<HousingPassiveItem> {

	public PassiveObject(House owner, int objId, int templateId) {
		super(owner, objId, templateId);
	}

	@Override
	public boolean canExpireNow() {
		return true;
	}

}
