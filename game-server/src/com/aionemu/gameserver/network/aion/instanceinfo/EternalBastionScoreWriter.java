package com.aionemu.gameserver.network.aion.instanceinfo;

import com.aionemu.gameserver.model.instance.instancescore.NormalScore;

import io.netty.buffer.ByteBuf;

/**
 * @author xTz, Estrayl
 */
public class EternalBastionScoreWriter extends InstanceScoreWriter<NormalScore> {

	public EternalBastionScoreWriter(NormalScore reward) {
		super(reward);
	}

	@Override
	public void writeMe(ByteBuf buf) {
		writeD(buf, instanceScore.getPoints());
		writeQ(buf, 0); // unk
		writeD(buf, instanceScore.getRank());
		writeD(buf, 0); // unk
		writeD(buf, instanceScore.getFinalAp());
		writeD(buf, instanceScore.getRewardItem1());
		writeD(buf, instanceScore.getRewardItem1Count());
		writeD(buf, instanceScore.getRewardItem2());
		writeD(buf, instanceScore.getRewardItem2Count());
		writeD(buf, instanceScore.getRewardItem3());
		writeD(buf, instanceScore.getRewardItem3Count());
		writeD(buf, instanceScore.getRewardItem4());
		writeD(buf, instanceScore.getRewardItem4Count());
	}

}
