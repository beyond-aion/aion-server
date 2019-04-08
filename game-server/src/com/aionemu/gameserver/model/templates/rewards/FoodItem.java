package com.aionemu.gameserver.model.templates.rewards;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.aionemu.commons.utils.Rnd;

@XmlAccessorType(XmlAccessType.FIELD)
public class FoodItem extends IdLevelReward {

	@Override
	public long getCount() {
		return Rnd.nextBoolean() ? 5 : 10;
	}

}
