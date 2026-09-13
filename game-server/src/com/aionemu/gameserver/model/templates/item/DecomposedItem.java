package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.ItemData;
import com.aionemu.gameserver.dataholders.StaticData;
import com.aionemu.gameserver.dataholders.loadingutils.StaticDataListener;

/**
 * One reward alternative of a {@link DecomposableSet}, weighted against its siblings.
 */
@XmlType(name = "DecomposedItem")
public class DecomposedItem {

	@XmlAttribute(name = "id")
	private int itemId;
	@XmlAttribute(name = "count")
	private int count = 1;
	@XmlAttribute(name = "chance")
	private float chance = 100;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		StaticData staticData = StaticDataListener.get(u);
		ItemData itemData = staticData != null ? staticData.itemData : DataManager.ITEM_DATA;
		if (itemData.getItemTemplate(itemId) == null)
			throw new IllegalArgumentException("Decomposable reward item ID is invalid: " + itemId);
		if (count <= 0)
			throw new IllegalArgumentException("Decomposable reward item [" + itemId + "] count (" + count + ") must be greater than 0");
		if (chance <= 0 || chance > 100)
			throw new IllegalArgumentException("Decomposable reward item [" + itemId + "] chance (" + chance + ") must be within (0, 100]");
	}

	public int getItemId() {
		return itemId;
	}

	public int getCount() {
		return count;
	}

	public float getChance() {
		return chance;
	}
}
