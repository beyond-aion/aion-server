package com.aionemu.gameserver.dao;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Equipment;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.items.storage.StorageType;

import javolution.util.FastTable;

/**
 * @author ATracer
 */
public abstract class InventoryDAO implements IDFactoryAwareDAO {

	/**
	 * @param ownerId
	 *          - account id for account warehouse, legion id for legion warehouse, player id for all other storage types
	 * @param storageType
	 * @return IStorage
	 */
	public abstract Storage loadStorage(int ownerId, StorageType storageType);

	public abstract List<Item> loadStorageDirect(int ownerId, StorageType storageType);

	/**
	 * @param player
	 * @return Equipment
	 */
	public abstract Equipment loadEquipment(Player player);

	/**
	 * @param playerId
	 * @return
	 */
	public abstract List<Item> loadEquipment(int playerId);

	public abstract boolean store(Player player);

	public abstract boolean store(Item item, Player player);

	public boolean store(Item item, int playerId) {
		return store(FastTable.of(item), playerId);
	}

	public abstract boolean store(List<Item> items, int playerId);

	public boolean store(Item item, Integer playerId, Integer accountId, Integer legionId) {
		return store(FastTable.of(item), playerId, accountId, legionId);
	}

	public abstract boolean store(List<Item> items, Integer playerId, Integer accountId, Integer legionId);

	/**
	 * @param playerId
	 */
	public abstract boolean deletePlayerItems(int playerId);

	public abstract void deleteAccountWH(int accountId);

	@Override
	public String getClassName() {
		return InventoryDAO.class.getName();
	}
}
