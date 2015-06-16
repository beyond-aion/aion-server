package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HousingJukeBox;

/**
 * @author Rolandas
 */
public class JukeBoxObject extends HouseObject<HousingJukeBox> {

	public JukeBoxObject(House owner, int objId, int templateId) {
		super(owner, objId, templateId);
	}

	@Override
	public boolean canExpireNow() {
		return true;
	}

}
