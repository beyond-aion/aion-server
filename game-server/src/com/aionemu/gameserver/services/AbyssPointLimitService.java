package com.aionemu.gameserver.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.templates.abyss.AbyssRankTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * Limits the AP a player can gain: from the same victim only a limited amount within a cooldown, and from all players only while the accumulated
 * AP is not above the rank cap. The accumulated cap and its decrease come from the killer's rank, the per-victim cap and cooldown from the victim's
 * rank, as in retail. The accumulated AP is stored with the player and decreases only while the player is online. Cooldowns are kept in memory and
 * survive a relog.
 */
public class AbyssPointLimitService {

	private static final long CLEANUP_INTERVAL_MILLIS = 3 * 60 * 1000;

	private final Map<Integer, ApLimits> limitsByPlayer = new ConcurrentHashMap<>();

	public static AbyssPointLimitService getInstance() {
		return SingletonHolder.instance;
	}

	private AbyssPointLimitService() {
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this::removeUnusedLimits, CLEANUP_INTERVAL_MILLIS, CLEANUP_INTERVAL_MILLIS);
	}

	/**
	 * Retail takes the accumulated cap from the killer's rank, but the per-victim cap and cooldown from the victim's rank. A victim without a rank
	 * (rank 0) has no per-victim limit, only the accumulated one. The victim record is updated even if the accumulated AP blocks the gain, so the
	 * cooldown restarts on every kill.
	 *
	 * @return True if the whole amount may be added.
	 */
	public boolean canGainAp(Player killer, int victimObjectId, int victimRankId, int ap) {
		AbyssRankTemplate killerRank = DataManager.ABYSS_RANK_TABLE.get(killer.getRace(), killer.getAbyssRank().getRank().getId());
		if (killerRank == null)
			return true;
		AbyssRankTemplate victimRank = victimRankId > 0 ? DataManager.ABYSS_RANK_TABLE.get(killer.getRace(), victimRankId) : null;
		long now = System.currentTimeMillis();
		PlayerCommonData pcd = killer.getCommonData();
		ApLimits limits = limitsByPlayer.computeIfAbsent(killer.getObjectId(), _ -> new ApLimits(now));
		boolean belowCap, victimLimitReached = false;
		synchronized (limits) {
			reduceAccumulatedAp(pcd, limits, now, killerRank);
			long maxFromPlayers = killerRank.getMaxFromAllUsers();
			belowCap = pcd.getApFromUsers() <= maxFromPlayers;
			if (belowCap)
				pcd.setApFromUsers(pcd.getApFromUsers() + ap);
			if (victimRank != null) {
				long cooldownEnd = now + victimRank.getDelayTime() * 1000L;
				VictimApLimit limit = limits.byVictim.get(victimObjectId);
				if (limit == null || limit.cooldownEnd < now) {
					limits.byVictim.put(victimObjectId, new VictimApLimit(cooldownEnd, ap));
					victimLimitReached = false;
				} else {
					limit.cooldownEnd = cooldownEnd;
					victimLimitReached = limit.given >= victimRank.getGiveMaxPerUser();
					if (!victimLimitReached)
						limit.given += ap;
				}
			}
		}
		if (!belowCap) // You cannot gain any Abyss Points for a while as you have gained too many Abyss Points in too short a period of time.
			PacketSendUtility.sendPacket(killer, SM_SYSTEM_MESSAGE.STR_CANNOT_GET_AP_TIMEBASE_LIMIT());
		if (victimLimitReached) // You cannot get any Abyss Point from the current target for a while.
			PacketSendUtility.sendPacket(killer, SM_SYSTEM_MESSAGE.STR_CANNOT_GET_AP_TARGET_LIMIT());
		return belowCap && !victimLimitReached;
	}

	public void onEnterWorld(Player player) {
		ApLimits limits = limitsByPlayer.computeIfAbsent(player.getObjectId(), _ -> new ApLimits(System.currentTimeMillis()));
		synchronized (limits) {
			limits.lastReduceTime = System.currentTimeMillis();
		}
	}

	/**
	 * Applies the decrease of the accumulated AP up to now, so the stored value is current.
	 */
	public void onLeaveWorld(Player player) {
		ApLimits limits = limitsByPlayer.get(player.getObjectId());
		if (limits == null)
			return;
		AbyssRankTemplate rank = DataManager.ABYSS_RANK_TABLE.get(player.getRace(), player.getAbyssRank().getRank().getId());
		if (rank == null)
			return;
		synchronized (limits) {
			reduceAccumulatedAp(player.getCommonData(), limits, System.currentTimeMillis(), rank);
		}
	}

	private void reduceAccumulatedAp(PlayerCommonData pcd, ApLimits limits, long now, AbyssRankTemplate rank) {
		long intervalMillis = rank.getMaxReduceInterval() * 1000L;
		if (intervalMillis <= 0 || rank.getMaxReduceAmount() <= 0)
			return;
		long intervals = (now - limits.lastReduceTime) / intervalMillis;
		if (intervals > 0) {
			pcd.setApFromUsers(Math.max(0, pcd.getApFromUsers() - intervals * rank.getMaxReduceAmount()));
			limits.lastReduceTime += intervals * intervalMillis;
		}
	}

	private void removeUnusedLimits() {
		long now = System.currentTimeMillis();
		limitsByPlayer.entrySet().removeIf(entry -> {
			ApLimits limits = entry.getValue();
			synchronized (limits) {
				limits.byVictim.values().removeIf(limit -> limit.cooldownEnd < now);
				return limits.byVictim.isEmpty() && World.getInstance().getPlayer(entry.getKey()) == null;
			}
		});
	}

	private static class ApLimits {

		private final Map<Integer, VictimApLimit> byVictim = new HashMap<>();
		private long lastReduceTime;

		private ApLimits(long now) {
			lastReduceTime = now;
		}
	}

	private static class VictimApLimit {

		private long cooldownEnd;
		private long given;

		private VictimApLimit(long cooldownEnd, long given) {
			this.cooldownEnd = cooldownEnd;
			this.given = given;
		}
	}

	private static class SingletonHolder {

		private static final AbyssPointLimitService instance = new AbyssPointLimitService();
	}
}
