package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.instance.instancescore.NormalScore;

/**
 * Created by Yeats on 01.05.2016.
 */
public class TheShugoEmperorsVaultScoreWriter extends InstanceScoreWriter<NormalScore> {

	public TheShugoEmperorsVaultScoreWriter(NormalScore reward) {
		super(reward);
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeD(buf, instanceScore.getPoints());
		writeD(buf, instanceScore.getBasicAp());
		writeD(buf, 0);
		writeD(buf, instanceScore.getRank());
		writeD(buf, instanceScore.getFinalAp());
		writeD(buf, 0);
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
