package com.aionemu.gameserver.network.aion.instanceinfo;

import com.aionemu.gameserver.model.instance.instancescore.LegionDominionScore;

import io.netty.buffer.ByteBuf;

/**
 * @author Yeats
 */
public class LegionDominionScoreWriter extends InstanceScoreWriter<LegionDominionScore> {

	public LegionDominionScoreWriter(LegionDominionScore reward) {
		super(reward);
	}

	@Override
	public void writeMe(ByteBuf buf) {
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
