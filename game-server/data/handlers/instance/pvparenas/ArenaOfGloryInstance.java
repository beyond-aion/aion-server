package instance.pvparenas;

import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.network.aion.instanceinfo.ArenaOfGloryScoreInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@InstanceID(300550000)
public class ArenaOfGloryInstance extends PvPArenaInstance {

	public ArenaOfGloryInstance(WorldMapInstance instance) {
		super(instance);
	}

	@Override
	public void onInstanceCreate() {
		killBonus = 1000;
		deathFine = -200;
		super.onInstanceCreate();
	}

	@Override
	protected void sendPacket() {
		instance.forEachPlayer(player -> PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(instance.getMapId(), new ArenaOfGloryScoreInfo(instanceReward, player.getObjectId()))));
	}

	@Override
	protected void reward() {
		int totalPoints = instanceReward.getTotalPoints();
		int size = instanceReward.getInstanceRewards().size();
		// 100 * (rate * size) * (playerScore / playersScore)
		float totalScoreAP = (59.952f * size) * 100;
		float totalScoreGP = 45.8f * size;
		// to do other formula
		float rankingRate = 0;
		float gpRankingRate = 0;
		if (size > 1) {
			rankingRate = (0.077f * (4 - size));
			gpRankingRate = (0.5f * (4 - size));
		}
		float totalRankingAP = 26500 - 26500 * rankingRate;
		float totalRankingGP = 200 - 200 * gpRankingRate;
		for (InstancePlayerReward playerReward : instanceReward.getInstanceRewards()) {
			PvPArenaPlayerReward reward = (PvPArenaPlayerReward) playerReward;
			if (!reward.isRewarded()) {
				float playerRate = 1;
				Player player = instance.getPlayer(playerReward.getOwnerId());
				if (player != null) {
					playerRate = Rates.get(player, RatesConfig.PVP_ARENA_GLORY_REWARD_RATES);
				}
				int score = reward.getScorePoints();
				float scoreRate = ((float) score / (float) totalPoints);
				int rank = instanceReward.getRound() > 1 ? instanceReward.getRank(score) : 3;
				float percent = reward.getParticipation();
				float generalRate = 0.167f + rank * 0.227f;
				float generalGpRate = 0.2f + rank * 0.237f;
				int basicAP = 0;
				int basicGP = 0;
				float rankingAP = totalRankingAP;
				float rankingGP = totalRankingGP;
				if (rank > 0) {
					rankingAP = rankingAP - rankingAP * generalRate;
					rankingGP = rankingGP - rankingGP * generalGpRate;
				}
				int scoreAP = (int) (totalScoreAP * scoreRate);
				int scoreGP = (int) (totalScoreGP * scoreRate);
				rankingAP *= percent;
				rankingGP *= percent;
				rankingAP *= playerRate;
				rankingGP *= playerRate;
				byte level = 0;
				if (player != null) {
					level = player.getLevel();
				}
				switch (rank) {
					case 0:
						rankingGP = 241;
						scoreGP = 54;
						if (level >= 56 && level <= 60) {
							reward.setGloriousInsignia(1);
							reward.setMithrilMedal(5);
						} else {
							reward.setGloriousInsignia(1);
							reward.setCeramiumMedal(3);
						}
						break;
					case 1:
						rankingGP = 135;
						scoreGP = 25;
						if (level >= 56 && level <= 60) {
							reward.setMithrilMedal(1);
							reward.setPlatinumMedal(3);
						} else {
							reward.setCeramiumMedal(1);
						}
						break;
					case 2:
						rankingGP = 92;
						scoreGP = 14;
						if (level >= 56 && level <= 60) {
							reward.setPlatinumMedal(3);
						} else {
							reward.setMithrilMedal(2);
						}
						break;
					case 3:
						rankingGP = 54;
						scoreGP = 6;
						reward.setLifeSerum(1);
						break;
				}
				reward.setBasicAP(basicAP);
				reward.setRankingAP((int) rankingAP);
				reward.setScoreAP(scoreAP);
				reward.setBasicGP(basicGP);
				reward.setRankingGP((int) rankingGP);
				reward.setScoreGP(scoreGP);
			}
		}
		super.reward();
	}

}
