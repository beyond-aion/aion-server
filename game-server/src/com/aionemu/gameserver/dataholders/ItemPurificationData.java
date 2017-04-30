package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.templates.item.purification.ItemPurificationTemplate;
import com.aionemu.gameserver.model.templates.item.purification.PurificationResultItem;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author Ranastic
 * @reworked Navyan
 */
@XmlRootElement(name = "item_purifications")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemPurificationData {

	@XmlElement(name = "item_purification")
	protected List<ItemPurificationTemplate> itemPurificationTemplates;
	@XmlTransient
	private TIntObjectHashMap<ItemPurificationTemplate> itemPurificationSets;
	@XmlTransient
	private Map<Integer, Map<Integer, PurificationResultItem>> possibleResultItems;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		itemPurificationSets = new TIntObjectHashMap<>();
		possibleResultItems = new HashMap<>();

		for (ItemPurificationTemplate purificationTemplate : itemPurificationTemplates) {
			itemPurificationSets.put(purificationTemplate.getBaseItemId(), purificationTemplate);

			possibleResultItems.put(purificationTemplate.getBaseItemId(), new HashMap<>());
			for (PurificationResultItem resultItem : purificationTemplate.getPurificationResultItems())
				possibleResultItems.get(purificationTemplate.getBaseItemId()).put(resultItem.getItemId(), resultItem);
		}
		itemPurificationTemplates = null;
	}

	/**
	 * @param itemSetId
	 * @return
	 */
	public ItemPurificationTemplate getItemPurificationTemplate(int itemSetId) {
		return itemPurificationSets.get(itemSetId);
	}

	public Map<Integer, PurificationResultItem> getResultItemMap(int baseItemId) {
		return possibleResultItems.get(baseItemId);
	}

	/**
	 * @return itemSets.size()
	 */
	public int size() {
		return itemPurificationSets.size();
	}
}
