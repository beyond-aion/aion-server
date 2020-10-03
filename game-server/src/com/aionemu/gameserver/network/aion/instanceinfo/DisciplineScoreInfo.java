package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancereward.PvPArenaReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;

/**
 * @author xTz
 */
public class DisciplineScoreInfo extends ArenaScoreInfo {

	private final Integer ownerObject;

	public DisciplineScoreInfo(PvPArenaReward arenaReward, Integer ownerObject, List<Player> players) {
		super(arenaReward, players);
		this.ownerObject = ownerObject;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		PvPArenaPlayerReward rewardedPlayer = arenaReward.getPlayerReward(ownerObject);

		writePlayerRewards(buf);
		if (arenaReward.isRewarded() && arenaReward.canRewarded() && rewardedPlayer != null) {
			writeAP(buf, rewardedPlayer);
			writeGP(buf, rewardedPlayer);
			writeD(buf, 186000130); // 186000130
			writeD(buf, rewardedPlayer.getBasicCrucible()); // basicRewardCrucibleIn
			writeD(buf, rewardedPlayer.getScoreCrucible()); // scoreRewardCrucibleIn
			writeD(buf, rewardedPlayer.getRankingCrucible()); // rankingRewardCrucibleIn
			writeD(buf, 186000137); // 186000137
			writeD(buf, rewardedPlayer.getBasicCourage()); // basicRewardCourageIn
			writeD(buf, rewardedPlayer.getScoreCourage()); // scoreRewardCourageIn
			writeD(buf, rewardedPlayer.getRankingCourage()); // rankingRewardCourageIn
			if (rewardedPlayer.getOpportunity() != 0) {
				writeD(buf, 186000165); // 186000165
				writeD(buf, rewardedPlayer.getOpportunity()); // opportunity token
			} else if (rewardedPlayer.getGloryTicket() != 0) {
				writeD(buf, 186000185); // 186000185
				writeD(buf, rewardedPlayer.getGloryTicket()); // glory ticket
			} else {
				writeD(buf, 0);
				writeD(buf, 0);
			}
			writeD(buf, 0);
			writeD(buf, 0);

		} else {
			writeB(buf, new byte[72]);
		}
		writeD(buf, arenaReward.getBuffId()); // instance buff id
		writeD(buf, 0); // unk
		writeD(buf, arenaReward.getRound()); // round
		writeD(buf, arenaReward.getCapPoints()); // cap points
		writeD(buf, 3); // possible rounds
		writeD(buf, 1); // posssible reward table
	}

}
