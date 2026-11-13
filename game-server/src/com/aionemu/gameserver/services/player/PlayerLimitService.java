package com.aionemu.gameserver.services.player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongPredicate;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.SellLimit;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.cron.CronService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Source, Neon
 */
public class PlayerLimitService {

	private static final ConcurrentHashMap<Integer, Long> sellLimit = new ConcurrentHashMap<>();

	public static boolean sellWithinLimit(Player player, long itemPrice, long itemCount, LongPredicate sell) {
		if (itemPrice < 0 || itemCount <= 0)
			return false;
		if (!CustomConfig.LIMITS_ENABLED || itemPrice == 0)
			return sell.test(itemCount);

		synchronized (player.getAccount()) {
			int accountId = player.getAccount().getId();
			long limit = sellLimit.computeIfAbsent(accountId, id -> SellLimit.getSellLimit(player));
			long possibleCount = Math.max(0, limit / itemPrice);
			if (CustomConfig.LIMITS_ENABLE_DYNAMIC_CAP && possibleCount < itemCount)
				possibleCount++;
			if (possibleCount == 0 || limit == 0) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DAY_CANNOT_SELL_NPC(limit));
				return false;
			}
			long useCount = Math.min(possibleCount, itemCount);
			if (!sell.test(useCount))
				return false;
			sellLimit.replace(accountId, limit, useCount > limit / itemPrice ? 0 : limit - itemPrice * useCount);
			return true;
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
