package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.enchants.EnchantList;
import com.aionemu.gameserver.model.enchants.EnchantStat;
import com.aionemu.gameserver.model.enchants.EnchantTemplateData;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;

/**
 *
 * @author xTz
 */
@XmlRootElement(name = "enchant_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnchantData {

	@XmlElement(name = "enchant_list")
	private List<EnchantList> enchantList;

	@XmlTransient
	HashMap<ItemGroup, HashMap<Integer, List<EnchantStat>>> templates = new HashMap<>();
	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (EnchantList enchant : enchantList) {
			ItemGroup group = enchant.getItemGroup();
			HashMap<Integer, List<EnchantStat>> map = new HashMap<>();
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

	public HashMap<Integer, List<EnchantStat>> getTemplates(ItemGroup group) {
		return templates.get(group);
	}

}
