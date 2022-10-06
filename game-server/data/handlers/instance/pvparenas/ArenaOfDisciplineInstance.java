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
@InstanceID(300360000)
public class ArenaOfDisciplineInstance extends DisciplineTrainingGroundsInstance {

	public ArenaOfDisciplineInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	protected void reward() {
		int totalPoints = instanceReward.getTotalPoints();
		int size = instanceReward.getPlayerRewards().size() == 1 ? (instanceReward.getRound() == 1 ? 1 : 2) : 2; // enemy left before start: low reward,
																																																							// else full
		// 100 * (rate * size) * (playerScore / playersScore)
		float totalAP = (3.292f * size) * 100; // to do config
		float totalCrucible = (1.964f * size) * 100; // to do config
		float totalCourage = (0.174f * size) * 100; // to do config
		for (PvPArenaPlayerReward playerReward : instanceReward.getPlayerRewards()) {
			if (!playerReward.isRewarded()) {
				Player player = instance.getPlayer(playerReward.getOwnerId());
				if (player == null) // player left
					continue;
				float playerRate = Rates.get(player, RatesConfig.PVP_ARENA_DISCIPLINE_REWARD_RATES);
				int score = playerReward.getScorePoints();
				float scoreRate = ((float) score / (float) totalPoints);
				int rank = instanceReward.getRank(score);
				float percent = playerReward.getParticipation();
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
				playerReward.setBasicAP(basicAP);
				playerReward.setRankingAP(rankingAP);
				playerReward.setScoreAP(scoreAP);
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
				playerReward.setBasicCrucible(basicCrI);
				playerReward.setRankingCrucible(rankingCrI);
				playerReward.setScoreCrucible(scoreCrI);
				int basicCoI = 0;
				basicCoI *= percent;
				// to do other formula
				int rankingCoI = 23;
				if (size > 1) {
					rankingCoI = rank == 0 ? 59 : 23;
				}
				rankingCoI *= percent;
				int scoreCoI = (int) (totalCourage * scoreRate);
				playerReward.setBasicCourage((int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(player, basicCoI));
				playerReward.setRankingCourage((int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(player, rankingCoI));
				playerReward.setScoreCourage((int) Rates.ARENA_COURAGE_INSIGNIA_COUNT.calcResult(player, scoreCoI));
				if (instanceReward.canRewardOpportunityToken(playerReward)) {
					playerReward.setOpportunity(4);
				}
				switch (rank) {
					case 0:
						playerReward.setRankingGP(40);
						playerReward.setScoreGP(11);
						playerReward.setBasicGP(0);
						break;
					case 1:
						playerReward.setRankingGP(13);
						playerReward.setScoreGP(11);
						playerReward.setBasicGP(0);
						break;
				}
			}
		}
		super.reward();
	}

}
