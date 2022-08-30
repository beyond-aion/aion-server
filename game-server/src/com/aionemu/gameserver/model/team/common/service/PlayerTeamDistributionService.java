package com.aionemu.gameserver.model.team.common.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.DropConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
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
	public static void doReward(TemporaryPlayerTeam<?> team, float damagePercent, Npc owner, AionObject winner) {
		if (team == null || owner == null) {
			return;
		}

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

		long expReward = StatFunctions.calculateExperienceReward(filteredStats.highestLevel, owner);

		float instanceApMultiplier = 1f;
		if (owner.isInInstance()) {
			instanceApMultiplier = owner.getPosition().getWorldMapInstance().getInstanceHandler().getInstanceApMultiplier();
		}

		for (Player member : filteredStats.players) {
			// dead players shouldn't receive AP/EP/DP
			if (member.isDead())
				continue;

			// Reward init
			long rewardXp = Math.round(expReward * member.getLevel() / (float) filteredStats.partyLvlSum);
			int rewardDp = StatFunctions.calculateDPReward(member, owner);
			float rewardAp = 1;

			// Players 10 levels below highest member get 0 reward.
			if (filteredStats.highestLevel - member.getLevel() >= 10) {
				rewardXp = 0;
				rewardDp = 0;
			}

			// Dmg percent correction
			rewardXp *= damagePercent;
			rewardDp *= damagePercent;
			rewardAp *= damagePercent;
			rewardAp *= instanceApMultiplier;

			member.getCommonData().addExp(rewardXp, Rates.XP_GROUP_HUNTING, owner.getObjectTemplate().getL10n());
			member.getCommonData().addDp(rewardDp);
			if (owner.getAi().ask(AIQuestion.SHOULD_REWARD_AP) && !(filteredStats.mentorCount > 0 && CustomConfig.MENTOR_GROUP_AP)) {
				rewardAp *= StatFunctions.calculatePvEApGained(member, owner);
				int ap = (int) rewardAp / filteredStats.players.size();
				if (ap >= 1) {
					AbyssPointsService.addAp(member, owner, ap);
				}
			}
		}
		if (owner.getAi().ask(AIQuestion.SHOULD_LOOT)) {
			// Give Drop
			Player mostDamagePlayer = owner.getAggroList().getMostPlayerDamageOfMembers(team.getMembers(), filteredStats.highestLevel);
			if (mostDamagePlayer == null) {
				return;
			}

			if (winner.equals(team) && (filteredStats.mentorCount == 0 || !owner.getAi().getName().equals("chest"))) {
				DropRegistrationService.getInstance().registerDrop(owner, mostDamagePlayer, filteredStats.highestLevel, filteredStats.players);
			}
		}
	}

	private static class PlayerTeamRewardStats implements Consumer<Player> {

		final List<Player> players = new ArrayList<>();
		final boolean disableRangeChecks;
		int partyLvlSum = 0;
		int highestLevel = 0;
		int mentorCount = 0;
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
					mentorCount++;
				} else {
					if (!hasLivingPlayer && !member.isDead())
						hasLivingPlayer = true;

					players.add(member);
					partyLvlSum += member.getLevel();
					if (member.getLevel() > highestLevel)
						highestLevel = member.getLevel();
				}
			}
		}
	}
}
