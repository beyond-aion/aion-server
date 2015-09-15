package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import javolution.util.FastTable;

import com.aionemu.gameserver.model.enchants.TemperingList;
import com.aionemu.gameserver.model.enchants.TemperingStat;
import com.aionemu.gameserver.model.enchants.TemperingTemplateData;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;

/**
 * @author xTz
 */
@XmlRootElement(name = "tempering_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class TemperingData {

	@XmlElement(name = "tempering_list")
	private List<TemperingList> temperingList;
	@XmlTransient
	HashMap<ItemGroup, HashMap<Integer, List<TemperingStat>>> templates = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (TemperingList tempering : temperingList) {
			ItemGroup group = tempering.getItemGroup();
			HashMap<Integer, List<TemperingStat>> map = new HashMap<>();
			templates.put(group, map);
			for (TemperingTemplateData data : tempering.getTemperingDatas()) {
				int level = data.getLevel();
				List<TemperingStat> stats = new FastTable<>();
				for (TemperingStat stat : data.getTemperingStats()) {
					stats.add(stat);
				}
				templates.get(group).put(Integer.valueOf(level), stats);
			}
		}
		temperingList.clear();
		temperingList = null;
	}

	public int size() {
		return templates.size();
	}

	public HashMap<Integer, List<TemperingStat>> getTemplates(ItemGroup group) {
		return templates.get(group);
	}

}
