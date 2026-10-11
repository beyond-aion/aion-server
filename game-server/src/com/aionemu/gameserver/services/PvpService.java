package com.aionemu.gameserver.services;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.controllers.attack.DamageInfo;
import com.aionemu.gameserver.controllers.attack.DamageList;
import com.aionemu.gameserver.controllers.attack.KillCounter;
import com.aionemu.gameserver.controllers.attack.TeamDamageList;
import com.aionemu.gameserver.custom.pvpmap.PvpMapService;
import com.aionemu.gameserver.dao.HeadhuntingDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.event.Headhunter;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.templates.bounty.BountyTemplate;
import com.aionemu.gameserver.model.templates.bounty.BountyType;
import com.aionemu.gameserver.model.templates.bounty.KillBountyTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.AbyssService;
import com.aionemu.gameserver.services.conquerorAndProtectorSystem.ConquerorAndProtectorService;
import com.aionemu.gameserver.services.event.EventService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.collections.Predicates;
import com.aionemu.gameserver.utils.stats.StatFunctions;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author Sarynth, Estrayl
 */
public class PvpService {

	private static final Logger log = LoggerFactory.getLogger("KILL_LOG");
	private static final float FIRST_ATTACKER_XP_SHARE = 0.1f;
	private final List<KillBountyTemplate> killBounties;
	private final Map<Integer, Headhunter> headhunters;

	private PvpService() {
		killBounties = DataManager.KILL_BOUNTY_DATA.getKillBounties();
		headhunters = HeadhuntingDAO.loadHeadhunters();
	}

	public static PvpService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private void sendBountyReward(Player player, BountyType type, int killScore) {
		for (KillBountyTemplate template : killBounties) {
			if (template.getBountyType() != type || template.getKillCount() != killScore)
				continue;
			if (template.getRaceCondition() != Race.PC_ALL && template.getRaceCondition() != player.getRace())
				continue;
			List<BountyTemplate> bounties = new ArrayList<>();
			if (template.isRandomReward())
				bounties.add(Rnd.get(template.getBounties()));
			else
				bounties.addAll(template.getBounties());

			for (BountyTemplate bounty : bounties)
				ItemService.addItem(player, bounty.getItemId(), bounty.getCount(), true,
					new ItemUpdatePredicate(ItemAddType.ITEM_COLLECT, ItemUpdateType.INC_CASH_ITEM));
		}
	}

	public void finalizeHeadhuntingSeason() {
		headhunters.clear();
	}

	public void doReward(Player victim) {
		doReward(victim, 1);
	}

	public synchronized Headhunter getHeadhunterById(final int objId) {
		Headhunter headhunter = headhunters.putIfAbsent(objId, new Headhunter(objId, 0, System.currentTimeMillis(), PersistentState.UPDATE_REQUIRED));
		return headhunter != null ? headhunter : headhunters.get(objId);
	}

	public void doReward(Player victim, float apWinMulti) {
		DamageList damageList = victim.getAggroList().getFinalDamageList();
		DamageInfo<Creature> mostDamage = damageList.getMostDamage();
		if (mostDamage == null || !(mostDamage.getAttacker() instanceof Player winner)) {
			PacketSendUtility.sendPacket(victim, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_MY_DEATH());
			TemporaryPlayerTeam<?> team = victim.getCurrentTeam();
			if (team != null)
				team.sendPacket(Predicates.Players.allExcept(victim), SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_FRIENDLY_DEATH(victim.getName()));
			AbyssService.announceHighRankedDeath(victim);
			return;
		}

		List<Player> killers = findMembersToCountKillFor(winner, victim);
		if (!killers.isEmpty()) {
			for (Player killer : killers) {
				killer.getAbyssRank().incrementAllKills();
				if (CustomConfig.ENABLE_KILL_REWARD) {
					int kills = killer.getAbyssRank().getAllKill();
					for (KillBountyTemplate template : killBounties) {
						if (template.getBountyType() == BountyType.PER_X_KILLS) {
							int killStep = template.getKillCount();
							if (kills % killStep == 0)
								sendBountyReward(killer, BountyType.PER_X_KILLS, killStep);
						}
					}
				}
				if (EventsConfig.ENABLE_HEADHUNTING && EventsConfig.HEADHUNTING_MAPS.contains(victim.getWorldId())) {
					int kills = getHeadhunterById(killer.getObjectId()).incrementAndGetKills();
					sendBountyReward(killer, BountyType.SEASONAL_KILLS, kills);
				}
			}
			updateKillQuests(killers, victim);
			if (killers.contains(winner)) { // rewards for winner only (group members are ignored)
				ConquerorAndProtectorService.getInstance().onKill(winner, victim);
				EventService.getInstance().onPvpKill(winner, victim);
			}
		}

		logKill(winner, victim, killers);

		// track how much of the total damage actually generated AP (ignoring Duels, Arena, NPCs), so the victim loses his AP based on that fraction
		int apRelevantDamage = 0;
		int totalDamage = damageList.getTotalDamage();

		TeamDamageList teamDamages = damageList.toTeamDamages();
		DamageInfo<AionObject> firstDamage = teamDamages.getFirstDamage();
		int victimXp = DataManager.PVP_EXP_TABLE.getExp(victim.getLevel());

		// Distribute AP to groups and players that had damage.
		for (DamageInfo<AionObject> damageInfo : teamDamages.getCreatureOrTeamDamages()) {
			Collection<Player> teamMembers = new ArrayList<>();
			AionObject attacker = damageInfo.getAttacker();
			if (attacker instanceof Player player && player.getRace() != victim.getRace())
				teamMembers.add(player);
			else if (attacker instanceof TemporaryPlayerTeam<?> team && team.getLeaderObject().getRace() != victim.getRace())
				teamMembers = team.getMembers();

			float xpShare = (1 - FIRST_ATTACKER_XP_SHARE) * victimXp * damageInfo.getDamage() / totalDamage;
			if (damageInfo == firstDamage)
				xpShare += (int) (victimXp * FIRST_ATTACKER_XP_SHARE);
			// Add damage last, so we don't include damage from same race. (Duels, Arena)
			if (rewardPlayerTeam(teamMembers, attacker instanceof TemporaryPlayerTeam, victim, damageInfo.getDamage(), totalDamage, apWinMulti, xpShare))
				apRelevantDamage += damageInfo.getDamage();
		}

		// Apply lost AP to defeated player
		final int apLost = StatFunctions.calculatePvPApLost(victim, winner);
		final int apActuallyLost = apLost * apRelevantDamage / totalDamage;

		if (apActuallyLost > 0)
			AbyssPointsService.addAp(victim, -apActuallyLost);

		// Announce that player has died.
		if (victim.isInInstance() && !PvpMapService.getInstance().isOnPvPMap(victim)) {
			PacketSendUtility.broadcastPacketAndReceive(victim, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_FRIENDLY_DEATH_TO_B(victim.getName(), winner.getName()));
			PacketSendUtility.sendPacket(victim, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_MY_DEATH());
		} else {
			PacketSendUtility.sendPacket(winner, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_HOSTILE_DEATH_TO_ME(victim.getName()));
			PacketSendUtility.sendPacket(victim, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_MY_DEATH_TO_B(winner.getName()));
			PacketSendUtility.broadcastPacket(victim, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_FRIENDLY_DEATH_TO_B(victim.getName(), winner.getName()), false,
				player -> !player.isEnemy(victim));
			PacketSendUtility.broadcastPacket(winner, SM_SYSTEM_MESSAGE.STR_MSG_COMBAT_HOSTILE_DEATH_TO_B(winner.getName(), victim.getName()), false,
				player -> player.isEnemy(victim));
			AbyssService.announceHighRankedDeath(victim);
		}
	}

	private List<Player> findMembersToCountKillFor(Player winner, Player victim) {
		TemporaryPlayerTeam<? extends TeamMember<Player>> group = winner.getCurrentGroup();
		List<Player> killers;
		if (group == null)
			killers = new ArrayList<>(Collections.singletonList(winner));
		else
			killers = group.getMembers();
		killers.removeIf(m -> !m.isOnline() || m.getRace() == victim.getRace() || !m.equals(winner) && !PositionUtil.isInRange(m, victim, 50));
		return killers;
	}

	private void logKill(Player winner, Player victim, List<Player> assistedGroup) {
		if (LoggingConfig.LOG_KILL) {
			if (assistedGroup.size() > 1 || assistedGroup.size() == 1 && !assistedGroup.contains(winner))
				log.info("[KILL] {} killed {} assisted by {}", winner, victim,
					assistedGroup.stream().filter(p -> !p.equals(winner)).map(String::valueOf).collect(Collectors.joining(",")));
			else
				log.info("[KILL] {} killed {}", winner, victim);
		}

		if (LoggingConfig.LOG_PL) {
			String ip1 = winner.getClientConnection().getIP();
			String mac1 = winner.getClientConnection().getMacAddress();
			String ip2 = victim.getClientConnection().getIP();
			String mac2 = victim.getClientConnection().getMacAddress();
			if (mac1 != null && mac2 != null) {
				if (ip1.equalsIgnoreCase(ip2) && mac1.equalsIgnoreCase(mac2)) {
					AuditLogger.log(winner, "possibly practicing AP sharing with " + victim + " same ip=" + ip1 + " and mac=" + mac1 + ".");
				} else if (mac1.equalsIgnoreCase(mac2)) {
					AuditLogger.log(winner, "possibly practicing AP sharing with " + victim + " same mac=" + mac1 + ".");
				}
			}
		}
	}

	/**
	 * @param xpShare
	 *          The PvP XP for the team's damage, before it is shared among the members and reduced by their level difference to the victim.
	 */
	private boolean rewardPlayerTeam(Collection<Player> teamMember, boolean isTeam, Player victim, int damage, int totalDamage, float apWinMulti,
		float xpShare) {
		List<Player> players = new ArrayList<>();
		int maxRank = 1;
		int maxLevel = 0;

		for (Player member : teamMember) {
			if (!member.isOnline() || member.isDead() || !PositionUtil.isInRange(member, victim, GroupConfig.GROUP_MAX_DISTANCE))
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
		int baseDpReward = StatFunctions.calculatePvpDpGained(victim, maxRank, maxLevel);
		float groupDamagePercentage = (float) damage / totalDamage;
		int apRewardPerMember = Math.round(baseApReward * groupDamagePercentage / players.size());
		int dpRewardPerMember = Math.round(baseDpReward * groupDamagePercentage / players.size());
		Map<Player, Integer> xpRewards = calculateXpRewards(players, isTeam, victim, xpShare);

		for (Player member : players) {
			int memberApGain = 1;
			int memberDpGain = 1;
			if (KillCounter.addKillFor(member.getObjectId(), victim.getObjectId()) < CustomConfig.MAX_DAILY_PVP_KILLS) {
				if (apRewardPerMember > 0)
					memberApGain = Rates.AP_PVP.calcResult(member, apRewardPerMember);
				if (dpRewardPerMember > 0) {
					memberDpGain = StatFunctions.adjustPvpDpGained(dpRewardPerMember, victim.getLevel(), member.getLevel());
					memberDpGain = Rates.DP_PVP.calcResult(member, memberDpGain);
				}

			}
			AbyssPointsService.addAp(member, victim, memberApGain);
			member.getCommonData().addDp(memberDpGain);
			// PvP XP has its own limits, independent of the kill count above
			int xpReward = xpRewards.get(member);
			if (xpReward > 0) {
				xpReward = (int) (member.getGameStats().getStat(StatEnum.BOOST_HUNTING_XP_RATE, 100).getCurrent() / 100f * xpReward);
				if (PvpExpLimitService.getInstance().tryGainPvpExp(member, victim, xpReward))
					member.getCommonData().addExp(xpReward, Rates.XP_PVP, victim.getName()); // rates are applied in addExp()
			}
		}
		return true;
	}

	/**
	 * A single player gets the XP share reduced by the level difference to the victim. A team's share is split by weights that decrease the further
	 * a member is below the team's highest level, then each part is reduced by the member's level difference to the victim.
	 */
	private Map<Player, Integer> calculateXpRewards(List<Player> players, boolean isTeam, Player victim, float xpShare) {
		Map<Player, Integer> xpRewards = new HashMap<>();
		if (!isTeam) {
			for (Player player : players)
				xpRewards.put(player, (int) (xpShare * DataManager.PVP_EXP_MOD_TABLE.getMultiplier(player.getLevel(), victim.getLevel())));
			return xpRewards;
		}
		int teamXp = xpShare > 0 ? Math.max(1, (int) xpShare) : 0;
		int highestLevel = players.stream().mapToInt(Player::getLevel).max().orElse(0);
		float totalWeight = 0;
		for (Player member : players)
			totalWeight += DataManager.PARTY_EXP_MOD_TABLE.getWeight(member.getLevel(), highestLevel);
		boolean isMentorTeam = players.stream().anyMatch(Player::isMentor);
		for (Player member : players) {
			float weight = DataManager.PARTY_EXP_MOD_TABLE.getWeight(member.getLevel(), highestLevel);
			float multiplier = DataManager.PVP_EXP_MOD_TABLE.getMultiplier(member.getLevel(), victim.getLevel());
			int xp = totalWeight > 0 ? (int) (weight / totalWeight * multiplier * teamXp) : 0;
			if (isMentorTeam)
				xp = (int) Math.min(xp, DataManager.MENTEE_EXP_LIMIT_TABLE.getExpLimit(member.getLevel()));
			xpRewards.put(member, xp);
		}
		return xpRewards;
	}

	private void updateKillQuests(List<Player> killers, Player victim) {
		List<ZoneInstance> zones = victim.findZones();
		for (Player p : killers) {
			for (ZoneInstance zone : zones)
				QuestEngine.getInstance().onKillInZone(new QuestEnv(victim, p, 0), zone);
			QuestEngine.getInstance().onKillInWorld(new QuestEnv(victim, p, 0), victim.getWorldId());
			QuestEngine.getInstance().onKillRanked(new QuestEnv(victim, p, 0), victim.getAbyssRank().getRank());
		}
	}

	public Map<Integer, Headhunter> getAllHeadhunters() {
		return headhunters;
	}

	public Headhunter getHeadhunter(final int hunterId) {
		return headhunters.get(hunterId);
	}

	private static final class SingletonHolder {

		static final PvpService INSTANCE = new PvpService();
	}
}
