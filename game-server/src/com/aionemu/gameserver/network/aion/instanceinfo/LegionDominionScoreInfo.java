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
	
	////fsc 0x79 ddddddddddddddd 301500000 1800000 3145728 900000 0 0 1 10 10 164002239 1 164002239 1 0 0
	//3145728 900000 0 0 0 20 10 164002239 1 164002239 2 0 0
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
