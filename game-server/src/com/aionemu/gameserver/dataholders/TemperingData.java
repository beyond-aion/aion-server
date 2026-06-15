package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.enchants.TemperingList;
import com.aionemu.gameserver.model.enchants.TemperingStat;
import com.aionemu.gameserver.model.enchants.TemperingTemplateData;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author xTz
 */
@XmlRootElement(name = "tempering_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class TemperingData {

	@XmlElement(name = "tempering_list")
	private List<TemperingList> temperingList;
	@XmlTransient
	Map<String, Map<Integer, List<TemperingStat>>> templates = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (TemperingList tempering : temperingList) {
			Map<Integer, List<TemperingStat>> map = new HashMap<>();
			templates.put(tempering.getItemGroup(), map);
			for (TemperingTemplateData data : tempering.getTemperingDatas())
				map.put(data.getLevel(), data.getTemperingStats());
		}
		temperingList = null;
	}

	public int size() {
		return templates.size();
	}

	public Map<Integer, List<TemperingStat>> getTemplates(ItemTemplate itemTemplate) {
		if (itemTemplate.getTemperingName() != null)
			return templates.get(itemTemplate.getTemperingName());
		else
			return templates.get(itemTemplate.getItemGroup().toString());
	}

}
