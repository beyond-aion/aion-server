package com.aionemu.gameserver.dataholders.loadingutils;

import jakarta.xml.bind.Unmarshaller;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.ItemData;
import com.aionemu.gameserver.dataholders.StaticData;

/**
 * Helper to get a reference to the {@linkplain StaticData} while being unmarshalled.
 * 
 * @author Neon
 */
public class StaticDataListener extends Unmarshaller.Listener {

	private final StaticData staticData;

	public StaticDataListener(StaticData staticData) {
		this.staticData = staticData;
	}

	public static StaticData get(Unmarshaller u) {
		return u.getListener() instanceof StaticDataListener staticDataListener ? staticDataListener.staticData : null;
	}

	public static ItemData getItemData(Unmarshaller u) {
		StaticData staticData = get(u);
		if (staticData != null) {
			StaticDataLoader.waitForItemTemplates(u);
			return staticData.itemData;
		} else {
			return DataManager.ITEM_DATA;
		}
	}
}
