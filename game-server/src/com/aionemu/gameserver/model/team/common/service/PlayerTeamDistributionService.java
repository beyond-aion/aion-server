package com.aionemu.gameserver.model.team.common.service;

import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RewardType;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.stats.StatFunctions;
import com.google.common.base.Predicate;

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
		PlayerTeamRewardStats filteredStats = new PlayerTeamRewardStats(owner);
		team.applyOnMembers(filteredStats);

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
			if (member.getLifeStats().isAlreadyDead())
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

			member.getCommonData().addExp(rewardXp, RewardType.GROUP_HUNTING, owner.getObjectTemplate().getNameId());
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

	private static class PlayerTeamRewardStats implements Predicate<Player> {

		final List<Player> players = new FastTable<>();
		int partyLvlSum = 0;
		int highestLevel = 0;
		int mentorCount = 0;
		boolean hasLivingPlayer = false;
		Npc owner;

		public PlayerTeamRewardStats(Npc owner) {
			this.owner = owner;
		}

		@Override
		public boolean apply(Player member) {
			if (member.isOnline() && PositionUtil.isInRange(member, owner, GroupConfig.GROUP_MAX_DISTANCE)) {
				QuestEngine.getInstance().onKill(new QuestEnv(owner, member, 0));

				if (member.isMentor()) {
					mentorCount++;
					return true;
				}

				if (!hasLivingPlayer && !member.getLifeStats().isAlreadyDead())
					hasLivingPlayer = true;

				players.add(member);
				partyLvlSum += member.getLevel();
				if (member.getLevel() > highestLevel)
					highestLevel = member.getLevel();
			}
			return true;
		}
	}
}
