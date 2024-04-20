package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.item.purification.ItemPurificationTemplate;
import com.aionemu.gameserver.model.templates.item.purification.PurificationResult;

/**
 * @author Ranastic, Navyan
 */
@XmlRootElement(name = "item_purifications")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemPurificationData {

	@XmlElement(name = "item_purification")
	protected List<ItemPurificationTemplate> itemPurificationTemplates;
	@XmlTransient
	private Map<Integer, ItemPurificationTemplate> itemPurificationSets;
	@XmlTransient
	private Map<Integer, Map<Integer, PurificationResult>> possibleResultItems;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		itemPurificationSets = new HashMap<>();
		possibleResultItems = new HashMap<>();

		for (ItemPurificationTemplate purificationTemplate : itemPurificationTemplates) {
			itemPurificationSets.put(purificationTemplate.getBaseItemId(), purificationTemplate);

			possibleResultItems.put(purificationTemplate.getBaseItemId(), new HashMap<>());
			for (PurificationResult resultItem : purificationTemplate.getPurificationResults())
				possibleResultItems.get(purificationTemplate.getBaseItemId()).put(resultItem.getResultItemId(), resultItem);
		}
		itemPurificationTemplates = null;
	}

	public ItemPurificationTemplate getItemPurificationTemplate(int itemSetId) {
		return itemPurificationSets.get(itemSetId);
	}

	public Map<Integer, PurificationResult> getResultItemMap(int baseItemId) {
		return possibleResultItems.get(baseItemId);
	}

	public int size() {
		return itemPurificationSets.size();
	}
}
