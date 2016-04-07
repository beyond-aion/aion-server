package com.aionemu.gameserver.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;
import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.controllers.attack.KillList;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.player.AbyssRank;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RewardType;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.group.PlayerFilters;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;
import com.aionemu.gameserver.model.templates.bounty.BountyTemplate;
import com.aionemu.gameserver.model.templates.bounty.BountyType;
import com.aionemu.gameserver.model.templates.bounty.KillBountyTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.AbyssService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.stats.StatFunctions;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author Sarynth
 * @reworked Estrayl
 */
public class PvpService {

	private static final Logger log = LoggerFactory.getLogger("KILL_LOG");
	private static final PvpService INSTANCE = new PvpService();
	private final List<KillBountyTemplate> killBounties;
	private Map<Integer, KillList> pvpKillLists;

	private PvpService() {
		killBounties = DataManager.KILL_BOUNTY_DATA.getKillBounties();
		pvpKillLists = new FastMap<>();
	}

	public static final PvpService getInstance() {
		return INSTANCE;
	}

	private int getKillsFor(int winnerId, int victimId) {
		KillList winnerKillList = pvpKillLists.get(winnerId);

		if (winnerKillList == null)
			return 0;
		return winnerKillList.getKillsFor(victimId);
	}

	private void addKillFor(int winnerId, int victimId) {
		KillList winnerKillList = pvpKillLists.get(winnerId);
		if (winnerKillList == null) {
			winnerKillList = new KillList();
			pvpKillLists.put(winnerId, winnerKillList);
		}
		winnerKillList.addKillFor(victimId);
	}

	public void sendBountyReward(Player player, BountyType type, int neededKills) {
		for (KillBountyTemplate template : killBounties) {
			if (template.getBountyType() != type || template.getKillCount() != neededKills)
				continue;
			List<BountyTemplate> bounties = new FastTable<>();
			if (type == BountyType.PER_X_KILLS) {
				bounties.add(template.getBounties().get(Rnd.get(template.getBounties().size())));
			} else {
				for (BountyTemplate bounty : template.getBounties())
					bounties.add(bounty);
			}
			for (BountyTemplate bounty : bounties)
				ItemService.addItem(player, bounty.getItemId(), bounty.getCount(), true, new ItemUpdatePredicate(ItemAddType.ITEM_COLLECT,
					ItemUpdateType.INC_CASH_ITEM));
		}
	}

	public void doReward(Player victim) {
		doReward(victim, 1);
	}

	public void doReward(Player victim, float apWinMulti) {
		// winner is the player that receives the kill count
		final Player winner = victim.getAggroList().getMostPlayerDamage();

		int totalDamage = victim.getAggroList().getTotalDamage();

		if (totalDamage == 0 || winner == null) {
			PacketSendUtility.sendPacket(victim, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_MY_DEATH());
			if (victim.isInGroup2()) {
				victim.getPlayerGroup2().sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_FRIENDLY_DEATH(victim.getName()), new PlayerFilters.ExcludePlayerFilter(victim));
			} else if (victim.isInAlliance2()) {
				victim.getPlayerAlliance2().sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_FRIENDLY_DEATH(victim.getName()), new PlayerFilters.ExcludePlayerFilter(victim));
			}
			announceDeath(victim);
			return;
		}


		// Add Player Kill to record.
		winner.getAbyssRank().setAllKill();
		// Pvp Kill Reward.
		if (CustomConfig.ENABLE_KILL_REWARD) {
			int kills = winner.getAbyssRank().getAllKill();
			for (KillBountyTemplate template : killBounties) {
				int killStep = template.getKillCount();
				if (kills % killStep == 0)
					sendBountyReward(winner, BountyType.PER_X_KILLS, killStep);
			}
		}

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

		// Distribute AP to groups and players that had damage.
		for (AggroInfo aggro : victim.getAggroList().getFinalDamageList(true)) {
			Collection<Player> teamMembers = new FastTable<>();
			AionObject attacker = aggro.getAttacker();
			if (attacker instanceof Player && ((Player) attacker).getRace() != victim.getRace())
				teamMembers.add((Player) attacker);
			else if (attacker instanceof PlayerGroup && ((PlayerGroup) attacker).getLeaderObject().getRace() != victim.getRace())
				teamMembers = ((PlayerGroup) attacker).getMembers();
			else if (attacker instanceof PlayerAlliance && ((PlayerAlliance) attacker).getLeaderObject().getRace() != victim.getRace())
				teamMembers = ((PlayerAlliance) attacker).getMembers();

			// Add damage last, so we don't include damage from same race. (Duels, Arena)
			if (rewardPlayerTeam(teamMembers, victim, totalDamage, aggro, apWinMulti))
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

		// Announce that player has died.
		if (victim.isInInstance() && victim.getWorldId() != 301220000) {
			PacketSendUtility.broadcastPacketAndReceive(victim, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_FRIENDLY_DEATH_TO_B(victim.getName(), winner.getName()));
			PacketSendUtility.sendPacket(victim, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_MY_DEATH());
		} else {
			PacketSendUtility.sendPacket(winner, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_HOSTILE_DEATH_TO_ME(victim.getName()));
			PacketSendUtility.sendPacket(victim, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_MY_DEATH_TO_B(winner.getName()));
			PacketSendUtility.broadcastPacket(victim, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_FRIENDLY_DEATH_TO_B(victim.getName(), winner.getName()), false, player -> !player.isEnemy(victim));
			PacketSendUtility.broadcastPacket(winner, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_HOSTILE_DEATH_TO_B(winner.getName(), victim.getName()), false, player -> player.isEnemy(victim));
			announceDeath(victim);
		}
	}

	// High ranked kill announce
	private void announceDeath(Player player) {
		AbyssRank ar = player.getAbyssRank();
		if (AbyssService.isOnPvpMap(player) && ar != null) {
			if (ar.getRank().getId() >= 9)
				AbyssService.rankedKillAnnounce(player);
		}
	}

	private boolean rewardPlayerTeam(Collection<Player> teamMember, Player victim, int totalDamage, AggroInfo info, float apWinMulti) {
		List<Player> players = new FastTable<>();
		int maxRank = 1;
		int maxLevel = 0;

		for (Player member : teamMember) {
			if (!member.isOnline() || member.getLifeStats().isAlreadyDead() || !MathUtil.isIn3dRange(member, victim, GroupConfig.GROUP_MAX_DISTANCE))
				continue;
			players.add(member);
			if (member.getLevel() > maxLevel)
				maxLevel = member.getLevel();
			if (member.getAbyssRank().getRank().getId() > maxRank)
				maxRank = member.getAbyssRank().getRank().getId();
		}
		// They are all dead or out of range.
		if (players.isEmpty())
			return false;

		float baseApReward = StatFunctions.calculatePvpApGained(victim, maxRank, maxLevel) * apWinMulti;
		int baseXpReward = StatFunctions.calculatePvpXpGained(victim, maxRank, maxLevel);
		int baseDpReward = StatFunctions.calculatePvpDpGained(victim, maxRank, maxLevel);
		float groupDamagePercentage = (float) info.getDamage() / totalDamage;
		int apRewardPerMember = Math.round(baseApReward * groupDamagePercentage / players.size());
		int xpRewardPerMember = Math.round(baseXpReward * groupDamagePercentage / players.size());
		int dpRewardPerMember = Math.round(baseDpReward * groupDamagePercentage / players.size());

		for (Player member : players) {
			int memberApGain = 1;
			int memberXpGain = 1;
			int memberDpGain = 1;
			if (this.getKillsFor(member.getObjectId(), victim.getObjectId()) < CustomConfig.MAX_DAILY_PVP_KILLS) {
				if (apRewardPerMember > 0) {
					try {
						memberApGain = Math.toIntExact(RewardType.AP_PLAYER.calcReward(member, apRewardPerMember));
					} catch (ArithmeticException ae) {
						log.error("Attempt to add a massive amount of ap to player " + member.getName() + " that overflows Integer.MAX_VALUE!");
					}
				}
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

	private void notifyKillQuests(Player winner, Player victim) {
		if (winner.getRace() == victim.getRace())
			return;

		List<Player> rewarded = new FastTable<Player>();
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
}
