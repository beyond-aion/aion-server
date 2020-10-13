package com.aionemu.gameserver.taskmanager.tasks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.taskmanager.AbstractPeriodicTaskManager;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Mr. Poke
 */
public class TemporaryTradeTimeTask extends AbstractPeriodicTaskManager {

	private final Map<Item, Collection<Integer>> items = new HashMap<>();
	private final Map<Integer, Item> itemById = new HashMap<>();

	/**
	 * @param period
	 */
	public TemporaryTradeTimeTask() {
		super(1000);
	}

	public static TemporaryTradeTimeTask getInstance() {
		return SingletonHolder._instance;
	}

	public void addTask(Item item, Collection<Integer> players) {
		writeLock();
		try {
			items.put(item, players);
			itemById.put(item.getObjectId(), item);
		} finally {
			writeUnlock();
		}
	}

	public boolean canTrade(Item item, int playerObjectId) {
		Collection<Integer> players = items.get(item);
		if (players == null)
			return false;
		return players.contains(playerObjectId);
	}

	public boolean hasItem(Item item) {
		readLock();
		try {
			return items.containsKey(item);
		} finally {
			readUnlock();
		}
	}

	public Item getItem(int objectId) {
		readLock();
		try {
			return itemById.get(objectId);
		} finally {
			readUnlock();
		}
	}

	@Override
	public void run() {
		writeLock();
		try {
			for (Iterator<Map.Entry<Item, Collection<Integer>>> iter = items.entrySet().iterator(); iter.hasNext();) {
				Map.Entry<Item, Collection<Integer>> entry = iter.next();
				Item item = entry.getKey();
				int time = (item.getTemporaryExchangeTime() - (int) (System.currentTimeMillis() / 1000));
				if (time <= 0) {
					for (int playerId : entry.getValue()) {
						Player player = World.getInstance().getPlayer(playerId);
						if (player != null)
							PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EXCHANGE_TIME_OVER(item.getL10n()));
					}
					item.setTemporaryExchangeTime(0);
					iter.remove();
					itemById.remove(item.getObjectId());
				}
			}
		} finally {
			writeUnlock();
		}
	}

	private static class SingletonHolder {

		protected static final TemporaryTradeTimeTask _instance = new TemporaryTradeTimeTask();
	}
}
