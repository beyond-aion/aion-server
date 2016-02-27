package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.instance.instancereward.LegionDominionReward;

/**
 * @author Yeats
 *
 */
public class LegionDominionScoreInfo extends InstanceScoreInfo {

	private final LegionDominionReward ldr;

	public LegionDominionScoreInfo(LegionDominionReward ldr) {
		this.ldr = ldr;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeD(buf, ldr.getPoints());
		writeD(buf, 0); // unk
		writeD(buf, 0); // unk
		writeD(buf, 0); // unk
		writeD(buf, ldr.getFinalGP());
		writeD(buf, ldr.getFinalAp());
		writeD(buf, ldr.getRewardItem1());
		writeD(buf, ldr.getRewardItem1Count());
		writeD(buf, ldr.getRewardItem2());
		writeD(buf, ldr.getRewardItem2Count());
		writeD(buf, ldr.getRewardItem3());
		writeD(buf, ldr.getRewardItem3Count());
		writeD(buf, ldr.getRewardItem4());
		writeD(buf, ldr.getRewardItem4Count());
	}
}
