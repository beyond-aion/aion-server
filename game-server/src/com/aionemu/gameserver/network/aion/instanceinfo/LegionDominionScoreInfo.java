package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.instance.instancereward.LegionDominionReward;

/**
 * @author Yeats
 */
public class LegionDominionScoreInfo extends InstanceScoreInfo<LegionDominionReward> {

	public LegionDominionScoreInfo(LegionDominionReward reward) {
		super(reward);
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeD(buf, reward.getPoints());
		writeD(buf, 0); // unk
		writeD(buf, 0); // unk
		writeD(buf, 0); // unk
		writeD(buf, reward.getFinalGP());
		writeD(buf, reward.getFinalAp());
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
