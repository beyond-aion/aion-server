package instance.pvparenas;

import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(300350000)
public class ArenaOfChaosInstance extends ChaosTrainingGroundsInstance {

	public ArenaOfChaosInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	protected void reward() {
		int totalPoints = instanceReward.getTotalPoints();
		int size = instanceReward.getPlayerRewards().size();
		// 100 * (rate * size) * (playerScore / playersScore)
		float totalScoreAP = (3.292f * size) * 100;
		float totalScoreCrucible = (1.964f * size) * 100;
		float totalScoreCourage = (0.225f * size) * 100;
		// to do other formula
		float rankingRate = 0;
		if (size > 1) {
			rankingRate = (0.077f * (8 - size));
		}
		float totalRankingAP = 1453 - 1453 * rankingRate;
		float totalRankingCrucible = 865 - 865 * rankingRate;
		float totalRankingCourage = 101 - 101 * rankingRate;
		for (PvPArenaPlayerReward playerReward : instanceReward.getPlayerRewards()) {
			if (!playerReward.isRewarded()) {
				Player player = instance.getPlayer(playerReward.getOwnerId());
				if (player == null) // player left
					continue;
				float playerRate = Rates.get(player, RatesConfig.PVP_ARENA_CHAOS_REWARD_RATES);
				int score = playerReward.getScorePoints();
				float scoreRate = ((float) score / (float) totalPoints);
				int rank = instanceReward.getRound() > 1 ? instanceReward.getRank(score) : 7;
				float percent = playerReward.getParticipation();
				float generalRate = 0.167f + rank * 0.095f;
				int basicAP = 200;
				float rankingAP = totalRankingAP;
				if (rank > 0) {
					rankingAP = rankingAP - rankingAP * generalRate;
				}
				int scoreAP = (int) (totalScoreAP * scoreRate);
				basicAP *= percent;
				rankingAP *= percent;
				rankingAP *= playerRate;
				playerReward.setBasicAP(basicAP);
				playerReward.setRankingAP((int) rankingAP);
				playerReward.setScoreAP(scoreAP);
				int basicCrI = 195;
				basicCrI *= percent;
				float rankingCrI = totalRankingCrucible;
				if (rank > 0) {
					rankingCrI = rankingCrI - rankingCrI * generalRate;
				}
				rankingCrI *= percent;
				rankingCrI *= playerRate;
				int scoreCrI = (int) (totalScoreCrucible * scoreRate);
				playerReward.setBasicCrucible(basicCrI);
				playerReward.setRankingCrucible((int) rankingCrI);
				playerReward.setScoreCrucible(scoreCrI);
				int basicCoI = 0;
				basicCoI *= percent;
				float rankingCoI = totalRankingCourage;
				if (rank > 0) {
					rankingCoI = rankingCoI - rankingCoI * generalRate;
				}
				rankingCoI *= percent;
				int scoreCoI = (int) (totalScoreCourage * scoreRate);
				playerReward.setBasicCourage((int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(player, basicCoI));
				playerReward.setRankingCourage((int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(player, (long) rankingCoI));
				playerReward.setScoreCourage((int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(player, scoreCoI));
				if (instanceReward.canRewardOpportunityToken(playerReward)) {
					playerReward.setOpportunity(4);
				}
				if (rank < 2) {
					playerReward.setGloryTicket(1);
				}
			}
		}
		super.reward();
	}

}
