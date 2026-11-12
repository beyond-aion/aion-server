package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.JAXBElement;
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
	private volatile Map<Integer, DecomposableItem> decomposableItemById;
	@XmlTransient
	private int overrideCount;

	synchronized void afterUnmarshal(Unmarshaller u, Object parent) {
		// JAXBUtil.deserialize should skip the preprocessing when called from Reload.java for example
		if (parent instanceof JAXBElement<?>)
			return;
		Map<Integer, DecomposableItem> decomposableItemById = new HashMap<>();
		Map<Integer, DecomposableItem> overrides = new HashMap<>();
		for (DecomposableItem template : decomposableItems) {
			Map<Integer, DecomposableItem> target = template.isOverride() ? overrides : decomposableItemById;
			if (target.putIfAbsent(template.getItemId(), template) != null) {
				if (template.isOverride())
					throw new IllegalArgumentException("Duplicate decomposable override item " + template.getItemId());
				else
					throw new IllegalArgumentException("Duplicate decomposable item " + template.getItemId() + " (override=\"true\" missing?)");
			}
		}
		// the files of the folder are merged in no particular order, so the custom entries replace the retail ones only after all are read
		decomposableItemById.putAll(overrides);
		decomposableItemById.values().removeIf(info -> info.getSets().isEmpty());
		this.decomposableItemById = decomposableItemById;
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

	public void setData(List<DecomposableItemsData> data) {
		this.decomposableItems = data.stream().flatMap(d -> d.decomposableItems.stream()).toList();
		afterUnmarshal(null, null);
	}
}
