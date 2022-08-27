package instance.pvparenas;

import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(300360000)
public class ArenaOfDisciplineInstance extends DisciplineTrainingGroundsInstance {

	public ArenaOfDisciplineInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	protected void reward() {
		int totalPoints = instanceReward.getTotalPoints();
		int size = instanceReward.getInstanceRewards().size() == 1 ? (instanceReward.getRound() == 1 ? 1 : 2) : 2; // enemy left before start: low reward,
																																																								// else full
		// 100 * (rate * size) * (playerScore / playersScore)
		float totalAP = (3.292f * size) * 100; // to do config
		float totalCrucible = (1.964f * size) * 100; // to do config
		float totalCourage = (0.174f * size) * 100; // to do config
		for (InstancePlayerReward playerReward : instanceReward.getInstanceRewards()) {
			PvPArenaPlayerReward reward = (PvPArenaPlayerReward) playerReward;
			if (!reward.isRewarded()) {
				Player player = instance.getPlayer(playerReward.getOwnerId());
				if (player == null) // player left
					continue;
				float playerRate = Rates.get(player, RatesConfig.PVP_ARENA_DISCIPLINE_REWARD_RATES);
				int score = reward.getScorePoints();
				float scoreRate = ((float) score / (float) totalPoints);
				int rank = instanceReward.getRank(score);
				float percent = reward.getParticipation();
				int basicAP = 200;
				// to do other formula
				int rankingAP = 431;
				if (size > 1) {
					rankingAP = rank == 0 ? 1108 : 431;
				}
				int scoreAP = (int) (totalAP * scoreRate);
				basicAP *= percent;
				rankingAP *= percent;
				rankingAP *= playerRate;
				reward.setBasicAP(basicAP);
				reward.setRankingAP(rankingAP);
				reward.setScoreAP(scoreAP);
				int basicCrI = 195;
				basicCrI *= percent;
				// to do other formula
				int rankingCrI = 256;
				if (size > 1) {
					rankingCrI = rank == 0 ? 660 : 256;
				}
				rankingCrI *= percent;
				rankingCrI *= playerRate;
				int scoreCrI = (int) (totalCrucible * scoreRate);
				reward.setBasicCrucible(basicCrI);
				reward.setRankingCrucible(rankingCrI);
				reward.setScoreCrucible(scoreCrI);
				int basicCoI = 0;
				basicCoI *= percent;
				// to do other formula
				int rankingCoI = 23;
				if (size > 1) {
					rankingCoI = rank == 0 ? 59 : 23;
				}
				rankingCoI *= percent;
				int scoreCoI = (int) (totalCourage * scoreRate);
				reward.setBasicCourage((int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(player, basicCoI));
				reward.setRankingCourage((int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(player, rankingCoI));
				reward.setScoreCourage((int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(player, scoreCoI));
				if (instanceReward.canRewardOpportunityToken(reward)) {
					reward.setOpportunity(4);
				}
				switch (rank) {
					case 0:
						reward.setRankingGP(40);
						reward.setScoreGP(11);
						reward.setBasicGP(0);
						break;
					case 1:
						reward.setRankingGP(13);
						reward.setScoreGP(11);
						reward.setBasicGP(0);
						break;
				}
			}
		}
		super.reward();
	}

}
