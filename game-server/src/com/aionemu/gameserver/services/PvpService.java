package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.List;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.controllers.attack.KillList;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RewardType;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;
import com.aionemu.gameserver.utils.stats.StatFunctions;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author Sarynth
 */
public class PvpService {

	private static Logger log = LoggerFactory.getLogger("KILL_LOG");

	public static final PvpService getInstance() {
		return SingletonHolder.instance;
	}

	private FastMap<Integer, KillList> pvpKillLists;

	private PvpService() {
		pvpKillLists = new FastMap<Integer, KillList>();
	}

	/**
	 * @param winnerId
	 * @param victimId
	 * @return
	 */
	private int getKillsFor(int winnerId, int victimId) {
		KillList winnerKillList = pvpKillLists.get(winnerId);

		if (winnerKillList == null)
			return 0;
		return winnerKillList.getKillsFor(victimId);
	}

	/**
	 * @param winnerId
	 * @param victimId
	 */
	private void addKillFor(int winnerId, int victimId) {
		KillList winnerKillList = pvpKillLists.get(winnerId);
		if (winnerKillList == null) {
			winnerKillList = new KillList();
			pvpKillLists.put(winnerId, winnerKillList);
		}
		winnerKillList.addKillFor(victimId);
	}

	/**
	 * @param victim
	 */
	public void doReward(Player victim) {
		// winner is the player that receives the kill count
		final Player winner = victim.getAggroList().getMostPlayerDamage();

		int totalDamage = victim.getAggroList().getTotalDamage();

		if (totalDamage == 0 || winner == null) {
			return;
		}

		// Add Player Kill to record.
		if (this.getKillsFor(winner.getObjectId(), victim.getObjectId()) < CustomConfig.MAX_DAILY_PVP_KILLS) {
			winner.getAbyssRank().setAllKill();
			int kills = winner.getAbyssRank().getAllKill();
			// Pvp Kill Reward.
			if (CustomConfig.ENABLE_KILL_REWARD) {
				if (kills % CustomConfig.KILLS_NEEDED1 == 1) {
					ItemService.addItem(winner, CustomConfig.REWARD1, 1);
					PacketSendUtility.sendMessage(winner, "Congratulations, you have won " + "[item: " + CustomConfig.REWARD1 + "] for having killed "
						+ CustomConfig.KILLS_NEEDED1 + " players !");
					log.info("[REWARD] Player [" + winner.getName() + "] win 2 [" + CustomConfig.REWARD1 + "]");
				}
				if (kills % CustomConfig.KILLS_NEEDED2 == 3) {
					ItemService.addItem(winner, CustomConfig.REWARD2, 1);
					PacketSendUtility.sendMessage(winner, "Congratulations, you have won " + "[item: " + CustomConfig.REWARD2 + "] for having killed "
						+ CustomConfig.KILLS_NEEDED2 + " players !");
					log.info("[REWARD] Player [" + winner.getName() + "] win 4 [" + CustomConfig.REWARD2 + "]");
				}
				if (kills % CustomConfig.KILLS_NEEDED3 == 5) {
					ItemService.addItem(winner, CustomConfig.REWARD3, 1);
					PacketSendUtility.sendMessage(winner, "Congratulations, you have won " + "[item: " + CustomConfig.REWARD3 + "] for having killed "
						+ CustomConfig.KILLS_NEEDED3 + " players !");
					log.info("[REWARD] Player [" + winner.getName() + "] win 6 [" + CustomConfig.REWARD3 + "]");
				}
			}
		}

		// Announce that player has died.
		PacketSendUtility.broadcastPacketAndReceive(victim, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_FRIENDLY_DEATH_TO_B(victim.getName(), winner.getName()));

		// Kill-log
		if (LoggingConfig.LOG_KILL)
			log.info("[KILL] Player [" + winner.getName() + "] killed [" + victim.getName() + "]");

		if (LoggingConfig.LOG_PL) {
			String ip1 = winner.getClientConnection().getIP();
			String mac1 = winner.getClientConnection().getMacAddress();
			String ip2 = victim.getClientConnection().getIP();
			String mac2 = victim.getClientConnection().getMacAddress();
			if (mac1 != null && mac2 != null) {
				if (ip1.equalsIgnoreCase(ip2) && mac1.equalsIgnoreCase(mac2)) {
					AuditLogger.info(winner, "Possible Power Leveling : " + winner.getName() + " with " + victim.getName() + "; same ip=" + ip1 + " and mac="
						+ mac1 + ".");
				} else if (mac1.equalsIgnoreCase(mac2)) {
					AuditLogger.info(winner, "Possible Power Leveling : " + winner.getName() + " with " + victim.getName() + "; same mac=" + mac1 + ".");
				}
			}
		}

		// Keep track of how much damage was dealt by players
		// so we can remove AP based on player damage...
		int playerDamage = 0;
		boolean success;

		// Distribute AP to groups and players that had damage.
		for (AggroInfo aggro : victim.getAggroList().getFinalDamageList(true)) {
			success = false;
			if (aggro.getAttacker() instanceof Player) {
				success = rewardPlayer(victim, totalDamage, aggro);
			} else if (aggro.getAttacker() instanceof PlayerGroup) {
				success = rewardPlayerGroup(victim, totalDamage, aggro);
			} else if (aggro.getAttacker() instanceof PlayerAlliance) {
				success = rewardPlayerAlliance(victim, totalDamage, aggro);
			}

			// Add damage last, so we don't include damage from same race. (Duels, Arena)
			if (success)
				playerDamage += aggro.getDamage();
		}

		SerialKillerService.getInstance().updateRank(winner, victim);

		SerialKillerService.getInstance().onKillSerialKiller(winner, victim);

		// notify Quest engine for winner + his group
		notifyKillQuests(winner, victim);

		// Apply lost AP to defeated player
		final int apLost = StatFunctions.calculatePvPApLost(victim, winner);
		final int apActuallyLost = apLost * playerDamage / totalDamage;

		if (apActuallyLost > 0)
			AbyssPointsService.addAp(victim, -apActuallyLost);

	}

	/**
	 * @param victim
	 * @param totalDamage
	 * @param aggro
	 * @return true if group is not same race
	 */
	private boolean rewardPlayerGroup(Player victim, int totalDamage, AggroInfo aggro) {
		// Reward Group
		PlayerGroup group = ((PlayerGroup) aggro.getAttacker());

		// Don't Reward Player of Same Faction.
		if (group.getRace() == victim.getRace())
			return false;

		// Find group members in range
		List<Player> players = new ArrayList<Player>();

		// Find highest rank and level in local group
		int maxRank = AbyssRankEnum.GRADE9_SOLDIER.getId();
		int maxLevel = 0;

		for (Player member : group.getMembers()) {
			if (!member.isOnline())
				continue;
			if (MathUtil.isIn3dRange(member, victim, GroupConfig.GROUP_MAX_DISTANCE)) {
				// Don't distribute AP to a dead player!
				if (!member.getLifeStats().isAlreadyDead()) {
					players.add(member);
					if (member.getLevel() > maxLevel)
						maxLevel = member.getLevel();
					if (member.getAbyssRank().getRank().getId() > maxRank)
						maxRank = member.getAbyssRank().getRank().getId();
				}
			}
		}

		// They are all dead or out of range.
		if (players.isEmpty())
			return false;

		int baseApReward = StatFunctions.calculatePvpApGained(victim, maxRank, maxLevel);
		int baseXpReward = StatFunctions.calculatePvpXpGained(victim, maxRank, maxLevel);
		int baseDpReward = StatFunctions.calculatePvpDpGained(victim, maxRank, maxLevel);
		float groupPercentage = (float) aggro.getDamage() / totalDamage;
		int apRewardPerMember = Math.round(baseApReward * groupPercentage / players.size());
		int xpRewardPerMember = Math.round(baseXpReward * groupPercentage / players.size());
		int dpRewardPerMember = Math.round(baseDpReward * groupPercentage / players.size());

		for (Player member : players) {
			int memberApGain = 1;
			int memberXpGain = 1;
			int memberDpGain = 1;
			if (this.getKillsFor(member.getObjectId(), victim.getObjectId()) < CustomConfig.MAX_DAILY_PVP_KILLS) {
				if (apRewardPerMember > 0)
					memberApGain = Math.round(RewardType.AP_PLAYER.calcReward(member, apRewardPerMember));
				if (xpRewardPerMember > 0)
					memberXpGain = Math.round(xpRewardPerMember * member.getRates().getXpPlayerGainRate());
				if (dpRewardPerMember > 0)
					memberDpGain = Math.round(StatFunctions.adjustPvpDpGained(dpRewardPerMember, victim.getLevel(), member.getLevel())
						* member.getRates().getDpPlayerRate());

			}
			AbyssPointsService.addAp(member, victim, memberApGain);
			member.getCommonData().addExp(memberXpGain, RewardType.PVP_KILL, victim.getName());
			member.getCommonData().addDp(memberDpGain);
			this.addKillFor(member.getObjectId(), victim.getObjectId());
		}

		return true;
	}

	/**
	 * @param victim
	 * @param totalDamage
	 * @param aggro
	 * @return true if group is not same race
	 */
	private boolean rewardPlayerAlliance(Player victim, int totalDamage, AggroInfo aggro) {
		// Reward Alliance
		PlayerAlliance alliance = ((PlayerAlliance) aggro.getAttacker());

		// Don't Reward Player of Same Faction.
		if (alliance.getLeaderObject().getRace() == victim.getRace())
			return false;

		// Find group members in range
		List<Player> players = new ArrayList<Player>();

		// Find highest rank and level in local group
		int maxRank = AbyssRankEnum.GRADE9_SOLDIER.getId();
		int maxLevel = 0;

		for (Player member : alliance.getMembers()) {
			if (!member.isOnline())
				continue;
			if (MathUtil.isIn3dRange(member, victim, GroupConfig.GROUP_MAX_DISTANCE)) {
				// Don't distribute AP to a dead player!
				if (!member.getLifeStats().isAlreadyDead()) {
					players.add(member);
					if (member.getLevel() > maxLevel)
						maxLevel = member.getLevel();
					if (member.getAbyssRank().getRank().getId() > maxRank)
						maxRank = member.getAbyssRank().getRank().getId();
				}
			}
		}

		// They are all dead or out of range.
		if (players.isEmpty())
			return false;

		int baseApReward = StatFunctions.calculatePvpApGained(victim, maxRank, maxLevel);
		int baseXpReward = StatFunctions.calculatePvpXpGained(victim, maxRank, maxLevel);
		int baseDpReward = StatFunctions.calculatePvpDpGained(victim, maxRank, maxLevel);
		float groupPercentage = (float) aggro.getDamage() / totalDamage;
		int apRewardPerMember = Math.round(baseApReward * groupPercentage / players.size());
		int xpRewardPerMember = Math.round(baseXpReward * groupPercentage / players.size());
		int dpRewardPerMember = Math.round(baseDpReward * groupPercentage / players.size());

		for (Player member : players) {
			int memberApGain = 1;
			int memberXpGain = 1;
			int memberDpGain = 1;
			if (this.getKillsFor(member.getObjectId(), victim.getObjectId()) < CustomConfig.MAX_DAILY_PVP_KILLS) {
				if (apRewardPerMember > 0)
					memberApGain = Math.round(RewardType.AP_PLAYER.calcReward(member, apRewardPerMember));
				if (xpRewardPerMember > 0)
					memberXpGain = Math.round(xpRewardPerMember * member.getRates().getXpPlayerGainRate());
				if (dpRewardPerMember > 0)
					memberDpGain = Math.round(StatFunctions.adjustPvpDpGained(dpRewardPerMember, victim.getLevel(), member.getLevel())
						* member.getRates().getDpPlayerRate());
			}
			AbyssPointsService.addAp(member, victim, memberApGain);
			member.getCommonData().addExp(memberXpGain, RewardType.PVP_KILL, victim.getName());
			member.getCommonData().addDp(memberDpGain);

			this.addKillFor(member.getObjectId(), victim.getObjectId());
		}

		return true;
	}

	/**
	 * @param victim
	 * @param totalDamage
	 * @param aggro
	 * @return true if player is not same race
	 */
	private boolean rewardPlayer(Player victim, int totalDamage, AggroInfo aggro) {
		// Reward Player
		Player winner = ((Player) aggro.getAttacker());

		// Don't Reward Player out of range/dead/same faction
		if (winner.getRace() == victim.getRace() || !winner.isOnline() || !MathUtil.isIn3dRange(winner, victim, GroupConfig.GROUP_MAX_DISTANCE)
			|| winner.getLifeStats().isAlreadyDead())
			return false;

		int baseApReward = 1;
		int baseXpReward = 1;
		int baseDpReward = 1;

		if (this.getKillsFor(winner.getObjectId(), victim.getObjectId()) < CustomConfig.MAX_DAILY_PVP_KILLS) {
			baseApReward = StatFunctions.calculatePvpApGained(victim, winner.getAbyssRank().getRank().getId(), winner.getLevel());
			baseXpReward = StatFunctions.calculatePvpXpGained(victim, winner.getAbyssRank().getRank().getId(), winner.getLevel());
			baseDpReward = StatFunctions.calculatePvpDpGained(victim, winner.getAbyssRank().getRank().getId(), winner.getLevel());
		}

		int apPlayerReward = Math.round(baseApReward * aggro.getDamage() / totalDamage);
		apPlayerReward = (int) RewardType.AP_PLAYER.calcReward(winner, apPlayerReward);
		int xpPlayerReward = Math.round(baseXpReward * winner.getRates().getXpPlayerGainRate() * aggro.getDamage() / totalDamage);
		int dpPlayerReward = Math.round(baseDpReward * winner.getRates().getDpPlayerRate() * aggro.getDamage() / totalDamage);

		AbyssPointsService.addAp(winner, victim, apPlayerReward);
		winner.getCommonData().addExp(xpPlayerReward, RewardType.PVP_KILL, victim.getName());
		winner.getCommonData().addDp(dpPlayerReward);
		this.addKillFor(winner.getObjectId(), victim.getObjectId());
		return true;
	}

	private void notifyKillQuests(Player winner, Player victim) {
		if (winner.getRace() == victim.getRace())
			return;

		List<Player> rewarded = new ArrayList<Player>();
		int worldId = victim.getWorldId();
		List<ZoneInstance> zones = victim.getPosition().getMapRegion().getZones(victim);

		if (winner.isInGroup2()) {
			rewarded.addAll(winner.getPlayerGroup2().getOnlineMembers());
		} else if (winner.isInAlliance2()) {
			rewarded.addAll(winner.getPlayerAllianceGroup2().getOnlineMembers());
		} else
			rewarded.add(winner);

		for (Player p : rewarded) {
			if (!MathUtil.isIn3dRange(p, victim, GroupConfig.GROUP_MAX_DISTANCE) || p.getLifeStats().isAlreadyDead())
				continue;

			for (ZoneInstance zone : zones) {
				QuestEngine.getInstance().onKillInZone(new QuestEnv(victim, p, 0, 0), zone.getAreaTemplate().getZoneName().name());
			}

			QuestEngine.getInstance().onKillInWorld(new QuestEnv(victim, p, 0, 0), worldId);
			QuestEngine.getInstance().onKillRanked(new QuestEnv(victim, p, 0, 0), victim.getAbyssRank().getRank());
		}
		rewarded.clear();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final PvpService instance = new PvpService();
	}
}
