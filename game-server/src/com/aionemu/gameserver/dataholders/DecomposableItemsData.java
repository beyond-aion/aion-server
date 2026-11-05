package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.item.DecomposableItem;

/**
 * @author antness
 */
@XmlRootElement(name = "decomposable_items")
@XmlAccessorType(XmlAccessType.FIELD)
public class DecomposableItemsData {

	@XmlElement(name = "decomposable")
	private List<DecomposableItem> decomposableItems;

	@XmlTransient
	private final Map<Integer, DecomposableItem> decomposableItemById = new HashMap<>();
	@XmlTransient
	private int overrideCount;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		decomposableItemById.clear();
		Map<Integer, DecomposableItem> overrides = new HashMap<>();
		for (DecomposableItem template : decomposableItems) {
			Map<Integer, DecomposableItem> target = template.isOverride() ? overrides : decomposableItemById;
			if (target.putIfAbsent(template.getItemId(), template) != null)
				throw new IllegalArgumentException("Duplicate decomposable item " + template.getItemId());
		}
		// the files of the folder are merged in no particular order, so the custom entries replace the retail ones only after all are read
		decomposableItemById.putAll(overrides);
		decomposableItemById.values().removeIf(info -> info.getSets().isEmpty());
		overrideCount = overrides.size();
		decomposableItems = null;
	}

	public int size() {
		return decomposableItemById.size();
	}

	public int overrideCount() {
		return overrideCount;
	}

	public DecomposableItem get(int itemId) {
		return decomposableItemById.get(itemId);
	}
}
