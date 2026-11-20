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
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.templates.bounty.BountyTemplate;
import com.aionemu.gameserver.model.templates.bounty.BountyType;
import com.aionemu.gameserver.model.templates.bounty.KillBountyTemplate;
import com.aionemu.gameserver.model.templates.npc.FirstAttackSplitRate;
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

		// AP of all killers (ignoring Duels, Arena, NPCs), the victim loses AP in proportion to it
		int killersAp = 0;
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

			// Add damage last, so we don't include damage from same race. (Duels, Arena)
			killersAp += rewardPlayerTeam(teamMembers, attacker instanceof TemporaryPlayerTeam, victim, damageInfo.getDamage(), totalDamage, apWinMulti,
				victimXp, damageInfo == firstDamage);
		}

		// Apply lost AP to defeated player
		final int apLost = StatFunctions.calculatePvPApLoss(victim, killersAp, apWinMulti);

		if (apLost > 0)
			AbyssPointsService.addAp(victim, -apLost);

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
	 * A player alone gets its part of the victim's AP, DP and XP times its level difference modifier. A team gets the part truncated as a whole and
	 * splits it by weights that decrease the further a member is below the team's highest level, then each member's part is reduced by its own level
	 * difference to the victim. The order of the float operations follows retail.
	 *
	 * @return AP of the team members after the rank difference modifier, see {@link AbyssPointsService#addKillAp}.
	 */
	private int rewardPlayerTeam(Collection<Player> teamMember, boolean isTeam, Player victim, int damage, int totalDamage, float apWinMulti,
		int victimXp, boolean firstAttacker) {
		List<Player> players = new ArrayList<>();

		for (Player member : teamMember) {
			if (!member.isOnline() || member.isDead() || !PositionUtil.isInRange(member, victim, GroupConfig.GROUP_MAX_DISTANCE))
				continue;
			players.add(member);
		}
		// They are all dead or out of range.
		if (players.isEmpty())
			return 0;

		FirstAttackSplitRate split = FirstAttackSplitRate.Split_Default;
		long apBase = StatFunctions.calculatePvpApBase(victim, apWinMulti);
		int dpBase = StatFunctions.getPvpDpBase();
		float apPart = StatFunctions.calculateRewardShare(apBase, split, damage, totalDamage, firstAttacker);
		float dpPart = StatFunctions.calculateRewardShare(dpBase, split, damage, totalDamage, firstAttacker);
		float xpPart = StatFunctions.calculateRewardShare(victimXp, split, damage, totalDamage, firstAttacker);
		// a team's AP, DP and XP never round down to nothing
		int teamAp = StatFunctions.calculateTeamPool(apBase, split, damage, totalDamage, firstAttacker, true);
		int teamDp = StatFunctions.calculateTeamPool(dpBase, split, damage, totalDamage, firstAttacker, true);
		int teamXp = StatFunctions.calculateTeamPool(victimXp, split, damage, totalDamage, firstAttacker, true);
		Map<Player, Float> shares = isTeam ? DataManager.PARTY_EXP_MOD_TABLE.getShares(players) : Map.of();
		boolean isMentorTeam = isTeam && players.stream().anyMatch(Player::isMentor);
		int killersAp = 0;

		for (Player member : players) {
			float pvpMod = DataManager.PVP_MOD_TABLE.getMultiplier(member.getLevel(), victim.getLevel());
			float xpMod = DataManager.PVP_EXP_MOD_TABLE.getMultiplier(member.getLevel(), victim.getLevel());
			// The kill counter is an optional extra cap; the retail AP limits are applied to every gain.
			boolean killLimitReached = CustomConfig.MAX_DAILY_PVP_KILLS > 0
				&& KillCounter.addKillFor(member.getObjectId(), victim.getObjectId()) >= CustomConfig.MAX_DAILY_PVP_KILLS;
			if (!killLimitReached && apBase > 0) {
				int ap = isTeam ? StatFunctions.calculateTeamMemberAp(teamAp, shares.get(member), pvpMod) : StatFunctions.calculatePvpApReward(member, victim, apPart);
				killersAp += AbyssPointsService.addKillAp(member, victim, victim.getAbyssRank().getRank().getId(), ap, Rates.AP_PVP);
			}
			int dp = isTeam ? StatFunctions.calculateTeamMemberDp(teamDp, shares.get(member), pvpMod, member.getLevel())
				: StatFunctions.calculatePvpDpReward(member, victim, dpPart);
			if (dp > 0)
				member.getCommonData().addDp(Rates.DP_PVP.calcResult(member, dp));
			// PvP XP has its own limits, independent of the kill count above
			int xpReward = isTeam ? (int) StatFunctions.calculateTeamMemberXp(teamXp, shares.get(member), xpMod) : (int) (xpPart * (100f * xpMod / 100f));
			if (isMentorTeam)
				xpReward = (int) Math.min(xpReward, DataManager.MENTEE_EXP_LIMIT_TABLE.getExpLimit(member.getLevel()));
			if (xpReward > 0) {
				xpReward = (int) Rates.XP_PVP.applyXpBoost(member, xpReward);
				long reposeBonus = member.getCommonData().useReposeEnergy(xpReward);
				// Not retail: a blocked kill gives no XP at all. Retail (User::GetExperience type 5) zeroes the XP and refunds the repose energy, but then
				// still adds the salvation energy, the world bonus and the legion bonus, all computed from the XP before the limit.
				if (PvpExpLimitService.getInstance().tryGainPvpExp(member, victim, xpReward + reposeBonus))
					member.getCommonData().addExp(xpReward, reposeBonus, Rates.XP_PVP, victim.getName()); // rates are applied in addExp()
				else
					member.getCommonData().returnReposeEnergy(reposeBonus);
			}
		}
		return killersAp;
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
