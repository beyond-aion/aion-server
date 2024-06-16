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
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.controllers.attack.KillCounter;
import com.aionemu.gameserver.custom.pvpmap.PvpMapService;
import com.aionemu.gameserver.dao.HeadhuntingDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.event.Headhunter;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
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
		Player winner = victim.getAggroList().getMostPlayerDamage();

		int totalDamage = victim.getAggroList().getTotalDamage();

		if (totalDamage == 0 || winner == null) {
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

		// Distribute AP to groups and players that had damage.
		for (AggroInfo aggro : victim.getAggroList().getFinalDamageList(true)) {
			Collection<Player> teamMembers = new ArrayList<>();
			AionObject attacker = aggro.getAttacker();
			if (attacker instanceof Player && ((Player) attacker).getRace() != victim.getRace())
				teamMembers.add((Player) attacker);
			else if (attacker instanceof PlayerGroup && ((PlayerGroup) attacker).getLeaderObject().getRace() != victim.getRace())
				teamMembers = ((PlayerGroup) attacker).getMembers();
			else if (attacker instanceof PlayerAlliance && ((PlayerAlliance) attacker).getLeaderObject().getRace() != victim.getRace())
				teamMembers = ((PlayerAlliance) attacker).getMembers();

			// Add damage last, so we don't include damage from same race. (Duels, Arena)
			if (rewardPlayerTeam(teamMembers, victim, totalDamage, aggro, apWinMulti))
				apRelevantDamage += aggro.getDamage();
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
				log.info("[KILL] " + winner + " killed " + victim + " assisted by "
					+ assistedGroup.stream().filter(p -> !p.equals(winner)).map(String::valueOf).collect(Collectors.joining(",")));
			else
				log.info("[KILL] " + winner + " killed " + victim);
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

	private boolean rewardPlayerTeam(Collection<Player> teamMember, Player victim, int totalDamage, AggroInfo info, float apWinMulti) {
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
			if (KillCounter.addKillFor(member.getObjectId(), victim.getObjectId()) < CustomConfig.MAX_DAILY_PVP_KILLS) {
				if (apRewardPerMember > 0) {
					try {
						memberApGain = Math.toIntExact(Rates.AP_PVP.calcResult(member, apRewardPerMember));
					} catch (ArithmeticException ae) {
						log.error("Attempt to add a massive amount of ap to player " + member.getName() + " that overflows Integer.MAX_VALUE!");
					}
				}
				if (xpRewardPerMember > 0)
					memberXpGain = xpRewardPerMember; // rates are applied in addExp()
				if (dpRewardPerMember > 0) {
					memberDpGain = StatFunctions.adjustPvpDpGained(dpRewardPerMember, victim.getLevel(), member.getLevel());
					memberDpGain = (int) Rates.DP_PVP.calcResult(member, memberDpGain);
				}

			}
			AbyssPointsService.addAp(member, victim, memberApGain);
			member.getCommonData().addExp(memberXpGain, Rates.XP_PVP, victim.getName());
			member.getCommonData().addDp(memberDpGain);
		}
		return true;
	}

	private void updateKillQuests(List<Player> killers, Player victim) {
		List<ZoneInstance> zones = victim.findZones();
		for (Player p : killers) {
			for (ZoneInstance zone : zones)
				QuestEngine.getInstance().onKillInZone(new QuestEnv(victim, p, 0), zone.getAreaTemplate().getZoneName().name());
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
