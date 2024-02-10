package instance.pvparenas;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.instance.playerreward.HarmonyGroupReward;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(300450000)
public class ArenaOfHarmonyInstance extends HarmonyTrainingGroundsInstance {

	private static final int REWARD_AP_BASE = 200;
	private static final float RANK_REWARD_RATE = 0.7f;
	private static final float SCORE_REWARD_RATE = 0.3f;
	private static final float WINNER_RANK_REWARD_RATE = 0.9f;
	private static final float LOSER_RANK_REWARD_RATE = 0.1f;

	public ArenaOfHarmonyInstance(WorldMapInstance instance) {
		super(instance);
	}

	private record BaseValuesPerPlayer(int apPerPlayer, int gpPerPlayer, int insigniaPerPlayer) {
	}

	@Override
	public void onInstanceCreate() {
		super.onInstanceCreate();
	}

	private BaseValuesPerPlayer getBaseValuesPerPlayer(int difficultyId) {
		// Extracted values from instant_dungeon_idarenapvp.xml (Retail Leak 4.6, also verified by different videos)
		return switch (difficultyId) {
			case 2 -> new BaseValuesPerPlayer(2200, 0, 22);
			case 3 -> new BaseValuesPerPlayer(3200, 0, 34);
			case 4 -> new BaseValuesPerPlayer(2500, 31, 34);
			default -> new BaseValuesPerPlayer(1200, 0, 12);
		};
	}

	private void setItemRewards(HarmonyGroupReward reward, boolean isWinner, int difficultyId) {
		switch (difficultyId) {
			case 1 -> {
				if (isWinner)
					reward.setRewardItem1(new RewardItem(188052179, 1));
			}
			case 2 -> {
				if (isWinner)
					reward.setRewardItem1(new RewardItem(188052180, 1));
			}
			case 3 -> {
				if (isWinner)
					reward.setRewardItem1(new RewardItem(188052185, 1));
				else
					reward.setRewardItem1(new RewardItem(188052181, 1));
			}
			case 4 -> {
				if (isWinner) {
					reward.setRewardItem1(new RewardItem(188052605, 1));
					reward.setRewardItem2(new RewardItem(188052482, 1));
				} else {
					reward.setRewardItem1(new RewardItem(188052606, 1));
				}
			}
		};
	}

	private int calcAndFloor(int number, float rate) {
		return (int) (number * rate);
	}

	@Override
	protected void reward() {
		if (instanceReward.getHarmonyGroupInside().isEmpty())
			return;

		BaseValuesPerPlayer baseValues = getBaseValuesPerPlayer(instanceReward.getHarmonyGroupInside().getFirst().getAgt().getDifficultId());

		int playerCount = instance.getPlayersInside().size();
		int totalAp = baseValues.apPerPlayer * playerCount;
		int totalGp = baseValues.gpPerPlayer * playerCount;
		int totalInsignia = baseValues.insigniaPerPlayer * playerCount;

		int rankAp = calcAndFloor(totalAp, RANK_REWARD_RATE);
		int rankGp = calcAndFloor(totalGp, RANK_REWARD_RATE);
		int rankInsignia = calcAndFloor(totalInsignia, RANK_REWARD_RATE);

		int winnerRankAp = calcAndFloor(rankAp, WINNER_RANK_REWARD_RATE);
		int winnerRankGp = calcAndFloor(rankGp, WINNER_RANK_REWARD_RATE);
		int winnerRankInsignia = calcAndFloor(rankInsignia, WINNER_RANK_REWARD_RATE);

		int loserRankAp = calcAndFloor(rankAp, LOSER_RANK_REWARD_RATE);
		int loserRankGp = calcAndFloor(rankGp, LOSER_RANK_REWARD_RATE);
		int loserRankInsignia = calcAndFloor(rankInsignia, LOSER_RANK_REWARD_RATE);

		int scoreAp = calcAndFloor(totalAp, SCORE_REWARD_RATE);
		int scoreGp = calcAndFloor(totalGp, SCORE_REWARD_RATE);
		int scoreInsignia = calcAndFloor(totalInsignia, SCORE_REWARD_RATE);

		int totalPoints = instanceReward.getTotalPoints();
		for (HarmonyGroupReward group : instanceReward.getGroups()) {
			int score = group.getPoints();
			int rank = instanceReward.getRound() > 1 ? instanceReward.getRank(score) : 1;
			boolean isWinner = rank == 0;
			float percent = group.getParticipation();
			// Score Formula to verify: floor(scoreAp * winnerKills * winnerPoints / (winnerKills * winnerPoints + loserKills * loserPoints))
			float scoreRate = (score / (float) totalPoints);

			group.setBasicAP(REWARD_AP_BASE);
			group.setRankingAP(isWinner ? winnerRankAp : loserRankAp);
			group.setScoreAP((int) (scoreAp * scoreRate));
			group.setBasicCourage(0);
			group.setRankingCourage(isWinner ? winnerRankInsignia : loserRankInsignia);
			group.setScoreCourage((int) (scoreInsignia * scoreRate));
			group.setBasicGP(0);
			group.setRankingGP(isWinner ? winnerRankGp : loserRankGp);
			group.setScoreGP((int) (scoreGp * scoreRate));
			setItemRewards(group, isWinner, group.getAgt().getDifficultId());
		}
		super.reward();
	}
}
