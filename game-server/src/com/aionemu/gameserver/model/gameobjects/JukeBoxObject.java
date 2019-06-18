package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.model.house.HouseRegistry;
import com.aionemu.gameserver.model.templates.housing.HousingJukeBox;

/**
 * @author Rolandas
 */
public class JukeBoxObject extends HouseObject<HousingJukeBox> {

	public JukeBoxObject(HouseRegistry registry, int objId, int templateId) {
		super(registry, objId, templateId);
	}

}
