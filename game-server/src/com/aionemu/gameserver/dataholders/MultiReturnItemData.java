package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.item.MultiReturnItem;
import com.aionemu.gameserver.model.templates.item.ReturnLocList;

/**
 * @author ginho1
 */
@XmlRootElement(name = "multi_return_item")
@XmlAccessorType(XmlAccessType.FIELD)
public class MultiReturnItemData {

	@XmlElement(name = "return_item")
	private List<MultiReturnItem> multiReturnItemTemplate;

	@XmlTransient
	private final Map<Integer, List<ReturnLocList>> returnLocList = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		returnLocList.clear();
		for (MultiReturnItem template : multiReturnItemTemplate)
			returnLocList.put(template.getId(), template.getReturnLocList());
		multiReturnItemTemplate = null;
	}

	public int size() {
		return returnLocList.size();
	}

	public List<ReturnLocList> getReturnLocListById(int id) {
		return returnLocList.get(id);
	}
}
