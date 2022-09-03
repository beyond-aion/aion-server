package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.instance.instancescore.LegionDominionScore;

/**
 * @author Yeats
 */
public class LegionDominionScoreWriter extends InstanceScoreWriter<LegionDominionScore> {

	public LegionDominionScoreWriter(LegionDominionScore reward) {
		super(reward);
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeD(buf, instanceScore.getPoints());
		writeD(buf, 0); // unk
		writeD(buf, 0); // unk
		writeD(buf, 0); // unk
		writeD(buf, instanceScore.getFinalGP());
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
