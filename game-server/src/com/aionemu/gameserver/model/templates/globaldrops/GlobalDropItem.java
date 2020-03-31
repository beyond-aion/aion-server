package com.aionemu.gameserver.model.templates.globaldrops;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.ItemData;
import com.aionemu.gameserver.dataholders.StaticData;
import com.aionemu.gameserver.dataholders.loadingutils.StaticDataListener;
import com.aionemu.gameserver.model.Chance;

/**
 * @author AionCool
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropItem")
public class GlobalDropItem implements Chance {

	@XmlAttribute(name = "id", required = true)
	private int itemId;
	@XmlAttribute(name = "min_count")
	private int minCount = 1;
	@XmlAttribute(name = "max_count")
	private int maxCount;
	@XmlAttribute(name = "chance")
	private float chance = 100f;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		StaticData staticData = StaticDataListener.get(u);
		ItemData itemData = staticData != null ? staticData.itemData : DataManager.ITEM_DATA;
		if (itemData.getItemTemplate(itemId) == null)
			throw new IllegalArgumentException("Global drop item ID " + itemId + " is invalid");
		if (minCount <= 0)
			throw new IllegalArgumentException("Global drop item [" + itemId + "] min_count (" + minCount + ") must be greater than 0");
		if (maxCount == 0)
			maxCount = minCount;
		else if (maxCount < minCount)
			throw new IllegalArgumentException(
				"Global drop item [" + itemId + "] max_count (" + maxCount + ") must be greater than or equal to min_count (" + minCount + ")");
	}

	public int getId() {
		return itemId;
	}

	public int getMinCount() {
		return minCount;
	}

	public int getMaxCount() {
		return maxCount;
	}

	@Override
	public float getChance() {
		return chance;
	}
}
