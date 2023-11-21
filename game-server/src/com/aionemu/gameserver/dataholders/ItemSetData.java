package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.itemset.ItemPart;
import com.aionemu.gameserver.model.templates.itemset.ItemSetTemplate;

/**
 * @author ATracer
 */
@XmlRootElement(name = "item_sets")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemSetData {

	@XmlElement(name = "itemset")
	protected List<ItemSetTemplate> itemsetList;

	@XmlTransient
	private Map<Integer, ItemSetTemplate> sets;
	@XmlTransient
	private Map<Integer, ItemSetTemplate> setItems;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		sets = new HashMap<>();
		setItems = new HashMap<>();

		for (ItemSetTemplate set : itemsetList) {
			sets.put(set.getId(), set);

			// Add reference to the ItemSetTemplate from
			for (ItemPart part : set.getItempart()) {
				setItems.put(part.getItemId(), set);
			}
		}
		itemsetList = null;
	}

	/**
	 * @param itemSetId
	 * @return
	 */
	public ItemSetTemplate getItemSetTemplate(int itemSetId) {
		return sets.get(itemSetId);
	}

	/**
	 * @param itemId
	 * @return
	 */
	public ItemSetTemplate getItemSetTemplateByItemId(int itemId) {
		return setItems.get(itemId);
	}

	/**
	 * @return itemSets.size()
	 */
	public int size() {
		return sets.size();
	}
}
