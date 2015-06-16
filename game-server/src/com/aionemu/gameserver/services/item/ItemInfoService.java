package com.aionemu.gameserver.services.item;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author ATracer
 */
public class ItemInfoService {

	public static final ItemQuality getQuality(int itemId) {
		return getItemTemplate(itemId).getItemQuality();
	}

	public static final int getNameId(int itemId) {
		return getItemTemplate(itemId).getNameId();
	}

	public static final ItemTemplate getItemTemplate(int itemId) {
		return DataManager.ITEM_DATA.getItemTemplate(itemId);
	}
}
