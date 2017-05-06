package com.aionemu.gameserver.model.team.legion;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.LegionStorageProxy;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemDeleteType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.world.World;

/**
 * @author Simple
 */
public class LegionWarehouse extends Storage {

	private Legion legion;
	private int currentWhUser;

	public LegionWarehouse(Legion legion) {
		super(StorageType.LEGION_WAREHOUSE);
		this.legion = legion;
		this.setLimit(legion.getWarehouseSlots());
	}

	public Legion getLegion() {
		return this.legion;
	}

	public void setOwnerLegion(Legion legion) {
		this.legion = legion;
	}

	/**
	 * Used to add kinah from successful sieges. StorageProxy should be normally used to act with.
	 */
	@Override
	public void increaseKinah(long amount) {
		Player player = currentWhUser == 0 ? null : World.getInstance().findPlayer(currentWhUser);
		new LegionStorageProxy(this, player).increaseKinah(amount);
	}

	@Override
	public void increaseKinah(long amount, ItemUpdateType updateType) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public boolean tryDecreaseKinah(long amount) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public boolean tryDecreaseKinah(long amount, ItemUpdateType updateType) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public void decreaseKinah(long amount) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public void decreaseKinah(long amount, ItemUpdateType updateType) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public long increaseItemCount(Item item, long count) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public long increaseItemCount(Item item, long count, ItemUpdateType updateType) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public long decreaseItemCount(Item item, long count) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public long decreaseItemCount(Item item, long count, ItemUpdateType updateType) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public long decreaseItemCount(Item item, long count, ItemUpdateType updateType, QuestStatus questStatus) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public Item add(Item item) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public Item add(Item item, ItemAddType addType) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public Item put(Item item) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public Item delete(Item item) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public Item delete(Item item, ItemDeleteType deleteType) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public boolean decreaseByItemId(int itemId, long count) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public boolean decreaseByItemId(int itemId, long count, QuestStatus questStatus) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public boolean decreaseByObjectId(int itemObjId, long count) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public boolean decreaseByObjectId(int itemObjId, long count, ItemUpdateType updateType) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public boolean decreaseByObjectId(int itemObjId, long count, QuestStatus questStatus) {
		throw new UnsupportedOperationException("LWH should be used behind proxy");
	}

	@Override
	public void setOwner(Player player) {
		throw new UnsupportedOperationException("LWH doesnt have owner");
	}

	public void setWhUser(int currentWhUser) {
		this.currentWhUser = currentWhUser;
	}

	public int getWhUser() {
		return currentWhUser;
	}

}
