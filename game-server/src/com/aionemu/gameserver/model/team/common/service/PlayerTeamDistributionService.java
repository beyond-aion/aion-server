package com.aionemu.gameserver.model.team.common.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.DropConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.controllers.attack.DamageInfo;
import com.aionemu.gameserver.controllers.attack.TeamDamageList;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.templates.npc.FirstAttackSplitRate;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.abyss.GloryPointsService;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.stats.StatFunctions;

/**
 * @author ATracer, nrg
 */
public class PlayerTeamDistributionService {

	/**
	 * This method will send a reward if a player is in a team
	 */
	public static void doReward(TemporaryPlayerTeam<?> team, int damage, Npc owner, AionObject winner, TeamDamageList teamDamageList) {
		// Find team's members and determine highest level
		boolean disableRangeChecks = DropConfig.DISABLE_RANGE_CHECK_MAPS.contains(owner.getPosition().getMapId());
		PlayerTeamRewardStats filteredStats = new PlayerTeamRewardStats(owner, disableRangeChecks);
		if (team instanceof PlayerAlliance alli && alli.isInLeague()) {
			alli.getLeague().getMembers().forEach(a -> a.forEach(filteredStats));
		} else {
			team.forEach(filteredStats);
		}

		// All non-mentors are not nearby or dead
		if (filteredStats.players.isEmpty() || !filteredStats.hasLivingPlayer) {
			return;
		}

		NpcTemplate template = owner.getObjectTemplate();
		FirstAttackSplitRate splitRate = template.getFirstAttackSplitRate();
		DamageInfo<AionObject> firstDamage = teamDamageList.getFirstDamage();
		boolean firstAttacker = firstDamage != null && firstDamage.getAttacker().equals(team);
		int totalDamage = teamDamageList.getTotalDamage();
		long teamXp = StatFunctions.calculateBaseExperienceReward(owner, damage, totalDamage, firstAttacker);
		// mentors gain nothing, but their share of the team's XP is lost
		long teamMentors = team.getMembers().stream().filter(Player::isMentor).count();
		teamXp = applyMentorPenalty(teamXp, teamMentors, team.size());
		// a team's AP and DP never round down to nothing
		int teamDp = StatFunctions.calculateTeamPool(template.getDp(), splitRate, damage, totalDamage, firstAttacker, true);
		boolean apAllowed = !(filteredStats.mentorsInRange > 0 && CustomConfig.MENTOR_GROUP_AP);
		int teamAp = apAllowed ? StatFunctions.calculateTeamPool(template.getAbyssPoint(), splitRate, damage, totalDamage, firstAttacker, true) : 0;
		int teamGp = StatFunctions.calculateTeamPool(template.getGloryPoint(), splitRate, damage, totalDamage, firstAttacker, false);
		float instanceApMultiplier = owner.getPosition().getWorldMapInstance().getInstanceHandler().getApMultiplier();
		// dead players shouldn't receive AP/EP/DP
		List<Player> livingMembers = filteredStats.players.stream().filter(p -> !p.isDead()).toList();
		Map<Player, Float> shares = DataManager.PARTY_EXP_MOD_TABLE.getShares(livingMembers);
		for (Player member : livingMembers) {
			float teamShare = shares.get(member);
			if (teamShare <= 0)
				continue;
			float levelMod = DataManager.PC_EXP_MOD_TABLE.getMultiplier(member.getLevel(), owner.getLevel());
			long rewardXp = StatFunctions.calculateTeamMemberXp(teamXp, teamShare, levelMod);
			if (teamMentors > 0)
				rewardXp = Math.min(rewardXp, DataManager.MENTEE_EXP_LIMIT_TABLE.getExpLimit(member.getLevel()));
			member.getCommonData().addExp(rewardXp, Rates.XP_GROUP_HUNTING, template.getL10n());
			if (teamDp > 0)
				member.getCommonData().addDp(StatFunctions.calculateTeamMemberDp(teamDp, teamShare, levelMod, member.getLevel()));
			if (teamAp > 0)
				AbyssPointsService.addKillAp(member, owner, template.getAbyssRank(),
					StatFunctions.calculateTeamMemberAp(teamAp, teamShare, levelMod * instanceApMultiplier), Rates.AP_PVE);
			int rewardGp = Rates.GP.calcResult(member, StatFunctions.calculateTeamMemberGp(teamGp, teamShare, levelMod));
			if (rewardGp > 0)
				GloryPointsService.addGp(member.getObjectId(), rewardGp);
		}
		if (owner.getAi().ask(AIQuestion.REWARD_LOOT)) {
			// Give Drop
			DamageInfo<Player> mostDamageMember = teamDamageList.getMostDamageByTeam(team);
			if (mostDamageMember == null) {
				return;
			}
			Player mostDamagePlayer = mostDamageMember.getAttacker();
			if (mostDamagePlayer.isMentor()) {
				for (Player member : team.getMembers()) {
					if (member.getLevel() == filteredStats.highestLevel)
						mostDamagePlayer = member;
				}
			}
			if (winner.equals(team) && (filteredStats.mentorsInRange == 0 || !owner.getAi().getName().equals("chest"))) {
				DropRegistrationService.getInstance().registerDrop(owner, mostDamagePlayer, filteredStats.highestLevel, filteredStats.players);
			}
		}
	}

	/**
	 * Mentors gain nothing, so their share of the team's XP is dropped before the rest is split among the members.
	 */
	static long applyMentorPenalty(long teamXp, long mentors, int teamSize) {
		return mentors > 0 ? teamXp - (long) ((float) mentors / teamSize * teamXp) : teamXp;
	}

	private static class PlayerTeamRewardStats implements Consumer<Player> {

		final List<Player> players = new ArrayList<>();
		final boolean disableRangeChecks;
		int highestLevel = 0;
		int mentorsInRange = 0;
		boolean hasLivingPlayer = false;
		Npc owner;

		public PlayerTeamRewardStats(Npc owner, boolean disableRangeChecks) {
			this.owner = owner;
			this.disableRangeChecks = disableRangeChecks;
		}

		@Override
		public void accept(Player member) {
			if (member.isOnline() && PositionUtil.isInRange(member, owner, disableRangeChecks ? 9999 : GroupConfig.GROUP_MAX_DISTANCE)) {
				QuestEngine.getInstance().onKill(new QuestEnv(owner, member, 0));

				if (member.isMentor()) {
					mentorsInRange++;
				} else {
					if (!hasLivingPlayer && !member.isDead())
						hasLivingPlayer = true;

					players.add(member);
					if (member.getLevel() > highestLevel)
						highestLevel = member.getLevel();
				}
			}
		}
	}
}
