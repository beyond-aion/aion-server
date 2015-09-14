package com.aionemu.gameserver.dao;

import java.util.Collections;
import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Equipment;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.items.storage.StorageType;

/**
 * @author ATracer
 */
public abstract class InventoryDAO implements IDFactoryAwareDAO {

	/**
	 * @param playerId
	 * @param storageType
	 * @return IStorage
	 */
	public abstract Storage loadStorage(int playerId, StorageType storageType);
	public abstract List<Item> loadStorageDirect(int playerId, StorageType storageType);
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

	public boolean store(Item item, int playerId){
		return store(Collections.singletonList(item), playerId);
	}

	public abstract boolean store(List<Item> items, int playerId);

	/**
	 * @param item
	 */
	public boolean store(Item item, Integer playerId, Integer accountId, Integer legionId){
		FastTable<Item> temp = new FastTable<>();
		temp.add(item);
		return store(temp, playerId, accountId, legionId);
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
