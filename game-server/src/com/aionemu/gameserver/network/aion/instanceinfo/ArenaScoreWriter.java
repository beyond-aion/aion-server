package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.instance.instancescore.PvPArenaScore;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.model.templates.rewards.ArenaRewardItem;
import com.aionemu.gameserver.model.templates.rewards.RewardItem;

/**
 * @author Neonm, Estrayl
 */
public class ArenaScoreWriter extends InstanceScoreWriter<PvPArenaScore> {

	protected final int ownerObjectId;
	private final boolean rewardTable;

	public ArenaScoreWriter(PvPArenaScore score, int ownerObjectId, boolean rewardTable) {
		super(score);
		this.ownerObjectId = ownerObjectId;
		this.rewardTable = rewardTable;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writePlayerScores(buf);
		writeOwnerRewards(buf);
		writeD(buf, 0); // Another instance buff id, possibly 'stageend_buff'
		writeD(buf, 0); // unk
		writeD(buf, instanceScore.getRound()); // round
		writeD(buf, instanceScore.getUpperScoreCap()); // cap points
		writeD(buf, 3); // possible rounds
		writeD(buf, rewardTable ? 1 : 0); // possible reward table
	}

	protected void writePlayerScores(ByteBuffer buf) {
		int playerCount = 0;
		for (PvPArenaPlayerReward apr : instanceScore.getPlayerRewards()) {
			writeD(buf, apr.getOwnerId());
			writeD(buf, apr.getPvPKills());
			writeD(buf, instanceScore.isRewarded() ? apr.getScorePoints() : apr.getPoints());
			writeD(buf, 0); // unk
			writeC(buf, 0); // unk
			writeC(buf, apr.getPlayerClass().getClassId());
			writeC(buf, 1); // unk
			writeC(buf, instanceScore.getRank(apr)); // top position
			writeD(buf, apr.getRemainingTime()); // instance buff time, also controls shield effect
			writeD(buf, instanceScore.isRewarded() ? apr.getTimeBonus() : 0);
			writeD(buf, apr.hasBoostMorale() ? instanceScore.getBuffId() : 0); // Instance Buff ID
			writeD(buf, 0); // unk
			writeH(buf, instanceScore.isRewarded() ? (short) (apr.getParticipation() * 100) : 0); // participation
			writeS(buf, apr.getPlayerName(), 54);
			playerCount++;
		}
		if (playerCount < 12)
			writeB(buf, new byte[92 * (12 - playerCount)]); // spaces
	}

	private void writeOwnerRewards(ByteBuffer buf) {
		PvPArenaPlayerReward arenaReward = instanceScore.getPlayerReward(ownerObjectId);
		if (instanceScore.isRewarded() && instanceScore.canReward() && arenaReward != null) {
			writeReward(buf, arenaReward.getAp());
			writeReward(buf, arenaReward.getGp());
			writeReward(buf, arenaReward.getCrucibleInsignia());
			writeReward(buf, arenaReward.getCourageInsignia());
			writeSimpleReward(buf, arenaReward.getRewardItem1());
			writeSimpleReward(buf, arenaReward.getRewardItem2());
		} else {
			writeB(buf, new byte[72]);
		}
	}

	private void writeReward(ByteBuffer buf, ArenaRewardItem rewardItem) {
		if (rewardItem.itemId() != 0)
			writeD(buf, rewardItem.itemId());
		writeD(buf, rewardItem.baseCount());
		writeD(buf, rewardItem.scoreCount());
		writeD(buf, rewardItem.rankingCount());
	}

	private void writeSimpleReward(ByteBuffer buf, RewardItem rewardItem) {
		if (rewardItem != null) {
			writeD(buf, rewardItem.getId());
			writeD(buf, (int) rewardItem.getCount());
		} else {
			writeD(buf, 0);
			writeD(buf, 0);
		}
	}
}
