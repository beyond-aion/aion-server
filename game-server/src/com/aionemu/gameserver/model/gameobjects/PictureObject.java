package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HousingPicture;

/**
 * @author Rolandas
 */
public class PictureObject extends HouseObject<HousingPicture> {

	public PictureObject(House owner, int objId, int templateId) {
		super(owner, objId, templateId);
	}

	@Override
	public void onUse(Player player) {

	}

	@Override
	public boolean canExpireNow() {
		return true;
	}

}
