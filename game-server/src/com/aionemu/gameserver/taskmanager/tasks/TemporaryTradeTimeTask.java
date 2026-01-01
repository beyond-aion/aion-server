package com.aionemu.gameserver.taskmanager.tasks;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

	private final Map<Item, Collection<Integer>> items = new ConcurrentHashMap<>();

	public TemporaryTradeTimeTask() {
		super(1000);
	}

	public static TemporaryTradeTimeTask getInstance() {
		return SingletonHolder._instance;
	}

	public void addTask(Item item, Collection<Integer> players) {
		items.put(item, players);
	}

	public boolean canTrade(Item item, int playerObjectId) {
		Collection<Integer> players = items.get(item);
		if (players == null)
			return false;
		return players.contains(playerObjectId);
	}

	@Override
	public void run() {
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
			}
		}
	}

	private static class SingletonHolder {

		protected static final TemporaryTradeTimeTask _instance = new TemporaryTradeTimeTask();
	}
}
