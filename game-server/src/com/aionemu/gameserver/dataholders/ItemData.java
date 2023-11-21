package com.aionemu.gameserver.dataholders;

import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.items.ItemMask;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.restriction.ItemCleanupTemplate;

/**
 * @author Luno
 */
@XmlRootElement(name = "item_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemData {

	@XmlElement(name = "item_template")
	private List<ItemTemplate> its;

	@XmlTransient
	private final Map<Integer, ItemTemplate> items = new HashMap<>();
	@XmlTransient
	private final Map<Integer, List<ItemTemplate>> manastones = new HashMap<>();
	@XmlTransient
	private final Map<Integer, List<ItemTemplate>> ancientManastones = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (ItemTemplate it : its) {
			items.put(it.getTemplateId(), it);
			if (it.getItemGroup() == ItemGroup.MANASTONE) {
				add(manastones, it, it.getLevel());
				addStonesForHigherLevels(manastones, it, it.getLevel());
			} else if (it.getItemGroup() == ItemGroup.SPECIAL_MANASTONE) {
				if (!it.getName().toLowerCase().contains("pvp"))
					add(ancientManastones, it, it.getLevel());
			}
		}
		its = null;
	}

	private void add(Map<Integer, List<ItemTemplate>> itemMap, ItemTemplate item, int manastoneLevel) {
		if (item.getName().startsWith("[")) // skip [Stamp], [Event], [Legion], [Legion reward], ...
			return;
		itemMap.compute(manastoneLevel, (level, items) -> {
			if (items == null)
				items = new ArrayList<>();
			items.add(item);
			return items;
		});
	}

	private void addStonesForHigherLevels(Map<Integer, List<ItemTemplate>> itemMap, ItemTemplate item, int manastoneLevel) {
		if (manastoneLevel == 60 && item.getTemplateId() == 167000563) // Manastone: Healing Boost +3
			add(itemMap, item, 70);
		else if (manastoneLevel == 10 || manastoneLevel == 30 || manastoneLevel == 50) {
			if (item.getItemQuality() == ItemQuality.COMMON || item.getItemQuality() == ItemQuality.RARE) {
				if (item.getName().contains("Manastone: Attack +")) {
					add(itemMap, item, manastoneLevel + 10); // add 10 as 20 / 30 as 40 / 50 as 60
					if (manastoneLevel == 50)
						add(itemMap, item, 70);
				}
			}
		}
	}

	public void cleanup() {
		for (ItemCleanupTemplate ict : DataManager.ITEM_CLEAN_UP.getList()) {
			ItemTemplate template = getItemTemplate(ict.getId());
			applyCleanup(template, ict.resultTrade(), ItemMask.TRADEABLE);
			applyCleanup(template, ict.resultSell(), ItemMask.SELLABLE);
			applyCleanup(template, ict.resultWH(), ItemMask.STORABLE_IN_WH);
			applyCleanup(template, ict.resultAccountWH(), ItemMask.STORABLE_IN_AWH);
			applyCleanup(template, ict.resultLegionWH(), ItemMask.STORABLE_IN_LWH);
		}
	}

	private void applyCleanup(ItemTemplate item, byte result, int mask) {
		switch (result) {
			case 1 -> item.modifyMask(true, mask);
			case 0 -> item.modifyMask(false, mask);
		}
	}

	public ItemTemplate getItemTemplate(int itemId) {
		return items.get(itemId);
	}

	public Collection<ItemTemplate> getItemTemplates() {
		return items.values();
	}

	public int size() {
		return items.size();
	}

	public List<ItemTemplate> getManastones(int level) {
		return manastones.get(level);
	}

	public List<ItemTemplate> getAncientManastones(int level) {
		return ancientManastones.get(level);
	}
}
