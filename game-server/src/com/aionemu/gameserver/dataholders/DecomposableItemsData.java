package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.item.DecomposableItemInfo;

/**
 * @author antness
 */
@XmlRootElement(name = "decomposable_items")
@XmlAccessorType(XmlAccessType.FIELD)
public class DecomposableItemsData {

	@XmlElement(name = "decomposable")
	private List<DecomposableItemInfo> decomposableItemsTemplates;

	@XmlTransient
	private final Map<Integer, DecomposableItemInfo> decomposableItemsInfo = new HashMap<>();
	@XmlTransient
	private int overrideCount;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		decomposableItemsInfo.clear();
		Map<Integer, DecomposableItemInfo> overrides = new HashMap<>();
		for (DecomposableItemInfo template : decomposableItemsTemplates) {
			Map<Integer, DecomposableItemInfo> target = template.isOverride() ? overrides : decomposableItemsInfo;
			if (target.putIfAbsent(template.getItemId(), template) != null)
				throw new IllegalArgumentException("Duplicate decomposable item " + template.getItemId());
		}
		// the files of the folder are merged in no particular order, so the custom entries replace the retail ones only after all are read
		decomposableItemsInfo.putAll(overrides);
		decomposableItemsInfo.values().removeIf(info -> info.getSets().isEmpty());
		overrideCount = overrides.size();
		decomposableItemsTemplates = null;
	}

	public int size() {
		return decomposableItemsInfo.size();
	}

	public int overrideCount() {
		return overrideCount;
	}

	public DecomposableItemInfo getInfoByItemId(int itemId) {
		return decomposableItemsInfo.get(itemId);
	}
}
