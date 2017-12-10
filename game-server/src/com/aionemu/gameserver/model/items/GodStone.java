package com.aionemu.gameserver.model.items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.templates.item.GodstoneInfo;

/**
 * @author ATracer
 */
public class GodStone extends ItemStone {

	private static final Logger log = LoggerFactory.getLogger(GodStone.class);

	private final GodstoneInfo godstoneInfo;
	private int activatedCount;

	public GodStone(Item parentItem, int activatedCount, int itemId, PersistentState state) {
		super(parentItem.getObjectId(), itemId, 0, state);
		this.godstoneInfo = DataManager.ITEM_DATA.getItemTemplate(itemId).getGodstoneInfo();
		this.activatedCount = activatedCount;
		if (godstoneInfo == null)
			log.warn("Godstone info is missing for item: " + itemId);
	}

	public GodstoneInfo getGodstoneInfo() {
		return godstoneInfo;
	}

	public void increaseActivatedCount() {
		activatedCount++;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public int getActivatedCount() {
		return activatedCount;
	}

}
