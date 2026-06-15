package com.aionemu.gameserver.model.team.legion;

import java.util.concurrent.atomic.AtomicInteger;

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

	private static final int DEFAULT_ROWS = 3; // hardcoded, as the client doesn't allow to change it
	private static final int SLOTS_PER_ROW = 8;
	private final AtomicInteger currentUser = new AtomicInteger();

	public LegionWarehouse(Legion legion) {
		super(StorageType.LEGION_WAREHOUSE);
		updateLimit(legion.getWarehouseExpansions());
	}

	/**
	 * Used to add kinah from successful sieges. StorageProxy should be normally used to act with.
	 */
	@Override
	public void increaseKinah(long amount) {
		int currentWhUser = getCurrentUser();
		Player player = currentWhUser == 0 ? null : World.getInstance().getPlayer(currentWhUser);
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

	public boolean unsetInUse(int playerObjId) {
		return currentUser.compareAndSet(playerObjId, 0);
	}

	public boolean setInUse(int playerObjId) {
		return currentUser.compareAndSet(0, playerObjId);
	}

	public int getCurrentUser() {
		return currentUser.get();
	}

	@Override
	public void setLimit(int limit) {
		throw new UnsupportedOperationException("Slot limit is controlled by the expansion level, use updateLimit() instead");
	}

	public void updateLimit(int warehouseExpansions) {
		if (warehouseExpansions < 0)
			throw new IllegalArgumentException();
		int rows = DEFAULT_ROWS + warehouseExpansions;
		super.setLimit(rows * SLOTS_PER_ROW);
	}
}
