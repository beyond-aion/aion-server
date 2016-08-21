package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.item.MultiReturnItem;
import com.aionemu.gameserver.model.templates.item.ReturnLocList;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author ginho1
 */
@XmlRootElement(name = "multi_return_item")
@XmlAccessorType(XmlAccessType.FIELD)
public class MultiReturnItemData {

	@XmlElement(name = "return_item")
	private List<MultiReturnItem> multiReturnItemTemplate;
	private TIntObjectHashMap<List<ReturnLocList>> returnLocList = new TIntObjectHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		returnLocList.clear();
		for (MultiReturnItem template : multiReturnItemTemplate)
			returnLocList.put(template.getId(), template.getReturnLocList());
	}

	public int size() {
		return returnLocList.size();
	}

	public List<ReturnLocList> getReturnLocListById(int id) {
		return returnLocList.get(id);
	}
}
