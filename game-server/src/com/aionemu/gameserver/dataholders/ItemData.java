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

import com.aionemu.gameserver.model.items.ItemMask;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.restriction.ItemCleanupTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author Luno
 */
@XmlRootElement(name = "item_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class ItemData {

	@XmlElement(name = "item_template")
	private List<ItemTemplate> its;

	@XmlTransient
	private TIntObjectHashMap<ItemTemplate> items;

	@XmlTransient
	Map<Integer, List<ItemTemplate>> manastones = new LinkedHashMap<>();
	@XmlTransient
	Map<Integer, List<ItemTemplate>> eventManastones = new LinkedHashMap<>();
	@XmlTransient
	Map<Integer, List<ItemTemplate>> stampManastones = new LinkedHashMap<>();
	@XmlTransient
	Map<Integer, List<ItemTemplate>> specialManastones = new LinkedHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		items = new TIntObjectHashMap<>();
		for (ItemTemplate it : its) {
			items.put(it.getTemplateId(), it);
			if (it.getItemGroup().equals(ItemGroup.MANASTONE)) {
				int level = it.getLevel();
				if (!manastones.containsKey(level)) {
					manastones.put(level, new ArrayList<>());
				}
				if (!eventManastones.containsKey(level)) {
					eventManastones.put(level, new ArrayList<>());
				}
				if (!stampManastones.containsKey(level)) {
					stampManastones.put(level, new ArrayList<>());
				}
				if (it.getName().toLowerCase().startsWith("[stamp]"))
					stampManastones.get(level).add(it);
				else if (it.getName().toLowerCase().startsWith("[event]"))
					eventManastones.get(level).add(it);
				else if (!it.getName().toLowerCase().startsWith("[legion]"))
					manastones.get(level).add(it);
			} else if (it.getItemGroup().equals(ItemGroup.SPECIAL_MANASTONE)) {
				int level = it.getLevel();
				if (!specialManastones.containsKey(level))
					specialManastones.put(level, new ArrayList<>());

				if (!it.getName().toLowerCase().startsWith("[stamp]"))
					specialManastones.get(level).add(it);
			}
		}
		its = null;
	}

	public void cleanup() {
		for (ItemCleanupTemplate ict : DataManager.ITEM_CLEAN_UP.getList()) {
			ItemTemplate template = items.get(ict.getId());
			applyCleanup(template, ict.resultTrade(), ItemMask.TRADEABLE);
			applyCleanup(template, ict.resultSell(), ItemMask.SELLABLE);
			applyCleanup(template, ict.resultWH(), ItemMask.STORABLE_IN_WH);
			applyCleanup(template, ict.resultAccountWH(), ItemMask.STORABLE_IN_AWH);
			applyCleanup(template, ict.resultLegionWH(), ItemMask.STORABLE_IN_LWH);
		}
	}

	private void applyCleanup(ItemTemplate item, byte result, int mask) {
		if (result != -1) {
			switch (result) {
				case 1:
					item.modifyMask(true, mask);
					break;
				case 0:
					item.modifyMask(false, mask);
					break;
			}
		}
	}

	public ItemTemplate getItemTemplate(int itemId) {
		return items.get(itemId);
	}

	/**
	 * @return items.size()
	 */
	public int size() {
		return items.size();
	}

	public Map<Integer, List<ItemTemplate>> getManastones() {
		return manastones;
	}

	public Map<Integer, List<ItemTemplate>> getEventManastones() {
		return eventManastones;
	}

	public Map<Integer, List<ItemTemplate>> getStampManastones() {
		return stampManastones;
	}

	public Map<Integer, List<ItemTemplate>> getSpecialManastones() {
		return specialManastones;
	}
}
