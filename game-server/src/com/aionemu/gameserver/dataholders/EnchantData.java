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

import com.aionemu.gameserver.model.enchants.EnchantList;
import com.aionemu.gameserver.model.enchants.EnchantStat;
import com.aionemu.gameserver.model.enchants.EnchantTemplateData;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author xTz
 */
@XmlRootElement(name = "enchant_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnchantData {

	@XmlElement(name = "enchant_list")
	private List<EnchantList> enchantList;

	@XmlTransient
	Map<String, Map<Integer, List<EnchantStat>>> templates = new LinkedHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (EnchantList enchant : enchantList) {
			String group = enchant.getItemGroup();
			Map<Integer, List<EnchantStat>> map = new LinkedHashMap<>();
			templates.put(group, map);
			for (EnchantTemplateData data : enchant.getEnchantDatas()) {
				int level = data.getLevel();
				List<EnchantStat> stats = new ArrayList<>();
				for (EnchantStat stat : data.getEnchantStats()) {
					stats.add(stat);
				}
				templates.get(group).put(Integer.valueOf(level), stats);
			}
		}
		enchantList.clear();
		enchantList = null;
	}

	public int size() {
		return templates.size();
	}

	public Map<Integer, List<EnchantStat>> getTemplates(ItemTemplate itemTemplate) {
		if (itemTemplate.getEnchantName() != null)
			return templates.get(itemTemplate.getEnchantName());
		else
			return templates.get(itemTemplate.getItemGroup().toString());
	}

}
