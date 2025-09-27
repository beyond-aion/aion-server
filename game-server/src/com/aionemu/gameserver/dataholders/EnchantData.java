package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

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
	Map<String, Map<Integer, List<EnchantStat>>> templates = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (EnchantList enchant : enchantList) {
			Map<Integer, List<EnchantStat>> map = new HashMap<>();
			templates.put(enchant.getItemGroup(), map);
			for (EnchantTemplateData data : enchant.getEnchantDatas())
				map.put(data.getLevel(), data.getEnchantStats());
		}
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
