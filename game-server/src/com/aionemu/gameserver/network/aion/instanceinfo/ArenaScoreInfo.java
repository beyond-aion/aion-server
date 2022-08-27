package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.instance.instancereward.PvPArenaReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;

/**
 * @author Neon
 */
public abstract class ArenaScoreInfo extends InstanceScoreInfo<PvPArenaReward> {

	protected final int ownerObjectId;
	private final boolean rewardTable;

	protected ArenaScoreInfo(PvPArenaReward reward, int ownerObjectId, boolean rewardTable) {
		super(reward);
		this.ownerObjectId = ownerObjectId;
		this.rewardTable = rewardTable;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writePlayerScores(buf);
		writeOwnerRewards(buf);
		writeD(buf, reward.getBuffId()); // instance buff id
		writeD(buf, 0); // unk
		writeD(buf, reward.getRound()); // round
		writeD(buf, reward.getCapPoints()); // cap points
		writeD(buf, 3); // possible rounds
		writeD(buf, rewardTable ? 1 : 0); // possible reward table
	}

	protected void writePlayerScores(ByteBuffer buf) {
		int playerCount = 0;
		for (PvPArenaPlayerReward playerReward : reward.getInstanceRewards()) {
			writeD(buf, playerReward.getOwnerId());
			writeD(buf, playerReward.getPvPKills());
			writeD(buf, reward.isRewarded() ? playerReward.getPoints() + playerReward.getTimeBonus() : playerReward.getPoints());
			writeD(buf, 0); // unk
			writeC(buf, 0); // unk
			writeC(buf, playerReward.getPlayerClass().getClassId());
			writeC(buf, 1); // unk
			writeC(buf, reward.getRank(playerReward.getScorePoints())); // top position
			writeD(buf, playerReward.getRemaningTime()); // instance buff time
			writeD(buf, reward.isRewarded() ? playerReward.getTimeBonus() : 0);
			writeD(buf, 0); // unk
			writeD(buf, 0); // unk
			writeH(buf, reward.isRewarded() ? (short) (playerReward.getParticipation() * 100) : 0); // participation
			writeS(buf, playerReward.getPlayerName(), 54);
			playerCount++;
		}
		if (playerCount < 12)
			writeB(buf, new byte[92 * (12 - playerCount)]); // spaces
	}

	protected void writeOwnerRewards(ByteBuffer buf) {
		PvPArenaPlayerReward rewardedPlayer = reward.getPlayerReward(ownerObjectId);
		if (reward.isRewarded() && reward.canRewarded() && rewardedPlayer != null) {
			writeAP(buf, rewardedPlayer);
			writeGP(buf, rewardedPlayer);
			writeD(buf, 186000130); // Crucible Insignia
			writeD(buf, rewardedPlayer.getBasicCrucible()); // basicRewardCrucibleIn
			writeD(buf, rewardedPlayer.getScoreCrucible()); // scoreRewardCrucibleIn
			writeD(buf, rewardedPlayer.getRankingCrucible()); // rankingRewardCrucibleIn
			writeD(buf, 186000137); // Courage Insignia
			writeD(buf, rewardedPlayer.getBasicCourage()); // basicRewardCourageIn
			writeD(buf, rewardedPlayer.getScoreCourage()); // scoreRewardCourageIn
			writeD(buf, rewardedPlayer.getRankingCourage()); // rankingRewardCourageIn
			if (rewardedPlayer.getOpportunity() != 0) {
				writeD(buf, 186000165); // Opportunity Token
				writeD(buf, rewardedPlayer.getOpportunity()); // opportunity token
			} else if (rewardedPlayer.getGloryTicket() != 0) {
				writeD(buf, 186000185); // Arena of Glory Ticket
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
	}

	protected void writeAP(ByteBuffer buf, PvPArenaPlayerReward playerReward) {
		writeD(buf, playerReward.getBasicAP());
		writeD(buf, playerReward.getScoreAP());
		writeD(buf, playerReward.getRankingAP());
	}

	protected void writeGP(ByteBuffer buf, PvPArenaPlayerReward playerReward) {
		writeD(buf, playerReward.getBasicGP());
		writeD(buf, playerReward.getScoreGP());
		writeD(buf, playerReward.getRankingGP());
	}
}
