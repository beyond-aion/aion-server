package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HousingEmblem;

/**
 * @author Rolandas
 */
public class EmblemObject extends HouseObject<HousingEmblem> {

	public EmblemObject(House owner, int objId, int templateId) {
		super(owner, objId, templateId);
	}

	@Override
	public boolean canExpireNow() {
		// TODO Auto-generated method stub
		return false;
	}

}
