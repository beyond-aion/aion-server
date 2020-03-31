package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.item.DecomposableItemInfo;
import com.aionemu.gameserver.model.templates.item.ExtractedItemsCollection;
import com.aionemu.gameserver.model.templates.item.ResultedItem;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author antness
 */
@XmlRootElement(name = "decomposable_items")
@XmlAccessorType(XmlAccessType.FIELD)
public class DecomposableItemsData {

	@XmlElement(name = "decomposable")
	private List<DecomposableItemInfo> decomposableItemsTemplates;
	private TIntObjectHashMap<List<ExtractedItemsCollection>> decomposableItemsInfo = new TIntObjectHashMap<>();
	private TIntObjectHashMap<List<ResultedItem>> selectableDecomposables = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		decomposableItemsInfo.clear();
		for (DecomposableItemInfo template : decomposableItemsTemplates) {
			List<ExtractedItemsCollection> itemGroups = template.getItemsCollections();
			if (itemGroups != null) {
				if (template.isIsSelectable()) {
					selectableDecomposables.put(template.getItemId(), itemGroups.get(0).getItems());
				} else {
					decomposableItemsInfo.put(template.getItemId(), itemGroups);
				}
			}
		}
		decomposableItemsTemplates = null;
	}

	public int size() {
		return decomposableItemsInfo.size();
	}

	public List<ResultedItem> getSelectableItems(int itemId) {
		List<ResultedItem> items = selectableDecomposables.get(itemId);
		return items == null ? null : new ArrayList<>(items);
	}

	public List<ExtractedItemsCollection> getInfoByItemId(int itemId) {
		return decomposableItemsInfo.get(itemId);
	}
}
