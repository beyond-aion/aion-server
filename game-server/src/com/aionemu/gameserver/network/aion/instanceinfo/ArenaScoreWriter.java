package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.instance.instancescore.PvPArenaScore;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;

/**
 * @author Neon
 */
public abstract class ArenaScoreWriter extends InstanceScoreWriter<PvPArenaScore> {

	protected final int ownerObjectId;
	private final boolean rewardTable;

	protected ArenaScoreWriter(PvPArenaScore score, int ownerObjectId, boolean rewardTable) {
		super(score);
		this.ownerObjectId = ownerObjectId;
		this.rewardTable = rewardTable;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writePlayerScores(buf);
		writeOwnerRewards(buf);
		writeD(buf, instanceScore.getBuffId()); // instance buff id
		writeD(buf, 0); // unk
		writeD(buf, instanceScore.getRound()); // round
		writeD(buf, instanceScore.getUpperScoreCap()); // cap points
		writeD(buf, 3); // possible rounds
		writeD(buf, rewardTable ? 1 : 0); // possible reward table
	}

	protected void writePlayerScores(ByteBuffer buf) {
		int playerCount = 0;
		for (PvPArenaPlayerReward playerReward : instanceScore.getPlayerRewards()) {
			writeD(buf, playerReward.getOwnerId());
			writeD(buf, playerReward.getPvPKills());
			writeD(buf, instanceScore.isRewarded() ? playerReward.getPoints() + playerReward.getTimeBonus() : playerReward.getPoints());
			writeD(buf, 0); // unk
			writeC(buf, 0); // unk
			writeC(buf, playerReward.getPlayerClass().getClassId());
			writeC(buf, 1); // unk
			writeC(buf, instanceScore.getRank(playerReward.getScorePoints())); // top position
			writeD(buf, playerReward.getRemaningTime()); // instance buff time
			writeD(buf, instanceScore.isRewarded() ? playerReward.getTimeBonus() : 0);
			writeD(buf, 0); // unk
			writeD(buf, 0); // unk
			writeH(buf, instanceScore.isRewarded() ? (short) (playerReward.getParticipation() * 100) : 0); // participation
			writeS(buf, playerReward.getPlayerName(), 54);
			playerCount++;
		}
		if (playerCount < 12)
			writeB(buf, new byte[92 * (12 - playerCount)]); // spaces
	}

	protected void writeOwnerRewards(ByteBuffer buf) {
		PvPArenaPlayerReward rewardedPlayer = instanceScore.getPlayerReward(ownerObjectId);
		if (instanceScore.isRewarded() && instanceScore.canRewarded() && rewardedPlayer != null) {
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
