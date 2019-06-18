package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.model.house.HouseRegistry;
import com.aionemu.gameserver.model.templates.housing.HousingEmblem;

/**
 * @author Rolandas
 */
public class EmblemObject extends HouseObject<HousingEmblem> {

	public EmblemObject(HouseRegistry registry, int objId, int templateId) {
		super(registry, objId, templateId);
	}

	@Override
	public boolean canExpireNow() {
		return false;
	}

}
