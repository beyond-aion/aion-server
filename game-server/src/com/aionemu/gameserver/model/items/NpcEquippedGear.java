package com.aionemu.gameserver.model.items;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.aionemu.gameserver.dataholders.loadingutils.adapters.NpcEquipmentList;
import com.aionemu.gameserver.dataholders.loadingutils.adapters.NpcEquippedGearAdapter;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;

/**
 * @author Luno
 */
@XmlJavaTypeAdapter(NpcEquippedGearAdapter.class)
public class NpcEquippedGear implements Iterable<Entry<ItemSlot, ItemTemplate>> {

	private Map<ItemSlot, ItemTemplate> items;
	private int mask;

	private NpcEquipmentList v;

	public NpcEquippedGear(NpcEquipmentList v) {
		this.v = v;
	}

	public int getItemsMask() {
		if (items == null)
			init();
		return mask;
	}

	@Override
	public Iterator<Entry<ItemSlot, ItemTemplate>> iterator() {
		if (items == null)
			init();
		return items.entrySet().iterator();
	}

	/**
	 * Here NPC equipment mask is initialized. All NPC slot masks should be lower than 65536
	 */
	@SuppressWarnings("lossy-conversions")
	public void init() {
		synchronized (this) {
			if (items == null) {
				items = new TreeMap<>();
				for (ItemTemplate item : v.items) {
					ItemSlot[] itemSlots = ItemSlot.getSlotsFor(item.getItemSlot());
					for (ItemSlot itemSlot : itemSlots) {
						if (items.get(itemSlot) == null) {
							items.put(itemSlot, item);
							mask |= itemSlot.getSlotIdMask();
							break;
						}
					}
				}
			}
			v = null;
		}
	}

	public ItemTemplate getItem(ItemSlot itemSlot) {
		return items != null ? items.get(itemSlot) : null;
	}

}
