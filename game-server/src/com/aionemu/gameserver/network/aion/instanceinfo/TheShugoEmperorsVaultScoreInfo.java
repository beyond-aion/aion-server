package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.instance.instancereward.NormalReward;

/**
 * Created by Yeats on 01.05.2016.
 */
public class TheShugoEmperorsVaultScoreInfo extends InstanceScoreInfo<NormalReward> {

	public TheShugoEmperorsVaultScoreInfo(NormalReward reward) {
		super(reward);
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeD(buf, reward.getPoints());
		writeD(buf, reward.getBasicAp());
		writeD(buf, 0);
		writeD(buf, reward.getRank());
		writeD(buf, reward.getFinalAp());
		writeD(buf, 0);
		writeD(buf, reward.getRewardItem1());
		writeD(buf, reward.getRewardItem1Count());
		writeD(buf, reward.getRewardItem2());
		writeD(buf, reward.getRewardItem2Count());
		writeD(buf, reward.getRewardItem3());
		writeD(buf, reward.getRewardItem3Count());
		writeD(buf, reward.getRewardItem4());
		writeD(buf, reward.getRewardItem4Count());
	}

}
