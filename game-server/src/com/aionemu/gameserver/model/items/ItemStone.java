package com.aionemu.gameserver.model.items;

import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.stats.calc.StatOwner;

/**
 * @author ATracer, Wakizashi
 */
public class ItemStone implements StatOwner, Persistable {

	private int itemObjId;

	private int itemId;

	private int slot;

	private PersistentState persistentState;

	public static enum ItemStoneType {
		MANASTONE,
		GODSTONE,
		FUSIONSTONE,
		IDIANSTONE;
	}

	public ItemStone(int itemObjId, int itemId, int slot, PersistentState persistentState) {
		this.itemObjId = itemObjId;
		this.itemId = itemId;
		this.slot = slot;
		this.persistentState = persistentState;
	}

	public int getItemObjId() {
		return itemObjId;
	}

	public int getItemId() {
		return itemId;
	}

	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	@Override
	@SuppressWarnings("fallthrough")
	public void setPersistentState(PersistentState persistentState) {
		switch (persistentState) {
			case DELETED:
				if (this.persistentState == PersistentState.NEW)
					this.persistentState = PersistentState.NOACTION;
				else
					this.persistentState = PersistentState.DELETED;
				break;
			case UPDATE_REQUIRED:
				if (this.persistentState == PersistentState.NEW)
					break;
			default:
				this.persistentState = persistentState;
		}
	}

}
