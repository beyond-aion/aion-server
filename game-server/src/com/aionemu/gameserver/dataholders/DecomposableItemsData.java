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

	void afterUnmarshal(Unmarshaller u, Object parent) {
		decomposableItemsInfo.clear();
		for (DecomposableItemInfo template : decomposableItemsTemplates) {
			if (!template.getSets().isEmpty())
				decomposableItemsInfo.put(template.getItemId(), template);
		}
		decomposableItemsTemplates = null;
	}

	public int size() {
		return decomposableItemsInfo.size();
	}

	public DecomposableItemInfo getInfoByItemId(int itemId) {
		return decomposableItemsInfo.get(itemId);
	}
}
