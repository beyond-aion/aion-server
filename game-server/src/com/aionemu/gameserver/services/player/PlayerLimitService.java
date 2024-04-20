package com.aionemu.gameserver.services.player;

import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.SellLimit;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Source, Neon
 */
public class PlayerLimitService {

	private static ConcurrentHashMap<Integer, Long> sellLimit = new ConcurrentHashMap<>();

	/**
	 * @return The number of items that were subtracted from the sell limit or 0 if limit is reached and did not change.
	 */
	public static long updateSellLimit(Player player, final long itemPrice, final long itemCount) {
		if (!CustomConfig.LIMITS_ENABLED || itemPrice == 0)
			return itemCount;

		int accountId = player.getAccount().getId();
		Long limit = sellLimit.get(accountId);
		if (limit == null) {
			limit = SellLimit.getSellLimit(player);
			sellLimit.putIfAbsent(accountId, limit);
		}

		if (itemPrice < 0 || itemCount <= 0)
			return 0;

		long possibleCount = Math.max(0, limit / itemPrice);
		if (CustomConfig.LIMITS_ENABLE_DYNAMIC_CAP && possibleCount < itemCount)
			possibleCount += 1;

		if (possibleCount == 0 || limit == 0) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DAY_CANNOT_SELL_NPC(limit));
			return 0;
		} else {
			long useCount = Math.min(possibleCount, itemCount);
			limit -= Math.min(limit, itemPrice * useCount);
			sellLimit.put(accountId, limit);
			return useCount;
		}
	}

	public void scheduleUpdate() {
		CronService.getInstance().schedule(() -> sellLimit.clear(), CustomConfig.LIMITS_UPDATE, true);
	}

	public static PlayerLimitService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {

		protected static final PlayerLimitService instance = new PlayerLimitService();
	}

}
