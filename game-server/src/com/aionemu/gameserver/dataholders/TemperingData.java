package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

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
	Map<String, Map<Integer, List<TemperingStat>>> templates = new LinkedHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (TemperingList tempering : temperingList) {
			String group = tempering.getItemGroup();
			Map<Integer, List<TemperingStat>> map = new LinkedHashMap<>();
			templates.put(group, map);
			for (TemperingTemplateData data : tempering.getTemperingDatas()) {
				int level = data.getLevel();
				List<TemperingStat> stats = new ArrayList<>();
				for (TemperingStat stat : data.getTemperingStats())
					stats.add(stat);

				templates.get(group).put(Integer.valueOf(level), stats);
			}
		}
		temperingList.clear();
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
