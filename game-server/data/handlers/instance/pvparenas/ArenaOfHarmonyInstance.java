package instance.pvparenas;

import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.instance.playerreward.HarmonyGroupReward;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(300450000)
public class ArenaOfHarmonyInstance extends HaramoniousTrainingCenterInstance {

	public ArenaOfHarmonyInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	protected void reward() {
		float totalScoreAP = (9.599f * 3) * 100;
		float totalScoreCourage = (0.1f * 3) * 100;
		int totalPoints = instanceReward.getTotalPoints();
		for (HarmonyGroupReward group : instanceReward.getGroups()) {
			int score = group.getPoints();
			int rank = instanceReward.getRound() > 1 ? instanceReward.getRank(score) : 1;
			float percent = group.getParticipation();
			float scoreRate = ((float) score / (float) totalPoints);
			int basicAP = 200;
			int rankingAP = 0;
			int basicGP = 0;
			int rankingGP = 0;
			int scoreGP = 0;
			basicAP *= percent;
			int basicCoI = 0;
			int rankingCoI = 0;
			basicCoI *= percent;
			int scoreAP = (int) (totalScoreAP * scoreRate);
			byte difficult = group.getAgt().getDifficultId();
			switch (rank) {
				case 0:
					rankingAP = 4681;
					rankingCoI = 49;
					basicGP = 0;
					rankingGP = 39;
					scoreGP = 13;
					group.setVictoryReward(1);
					if (difficult == 4) {
						group.setArenaSuperiorSupply(1);
					} else {
						group.setArenaSupply(1);
					}
					break;
				case 1:
					rankingAP = 1887;
					rankingCoI = 20;
					basicGP = 0;
					rankingGP = 3;
					scoreGP = 3;
					group.setConsolationReward(1);
					break;
			}
			rankingAP *= percent;
			rankingCoI *= percent;
			int scoreCoI = (int) (totalScoreCourage * scoreRate);
			group.setBasicAP(basicAP);
			group.setRankingAP(rankingAP);
			group.setScoreAP(scoreAP);
			group.setBasicCourage(basicCoI); // player based custom rates apply later, since these are group rewards
			group.setRankingCourage(rankingCoI);
			group.setScoreCourage(scoreCoI);
			group.setBasicGP(basicGP);
			group.setRankingGP(rankingGP);
			group.setScoreGP(scoreGP);
		}
		super.reward();
	}
}
