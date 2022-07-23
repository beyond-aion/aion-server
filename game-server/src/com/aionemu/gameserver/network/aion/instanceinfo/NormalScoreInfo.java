package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.instance.instancereward.NormalReward;

/**
 * @author xTz
 */
public class NormalScoreInfo extends InstanceScoreInfo {

	private final NormalReward nr;

	public NormalScoreInfo(NormalReward nr) {
		this.nr = nr;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeD(buf, nr.getPoints());
		writeD(buf, nr.getBasicAp());
		writeD(buf, 0); // Possibly bonus AP
		writeD(buf, nr.getRank());
		writeD(buf, nr.getFinalAp());
		writeD(buf, nr.getRewardItem1());
		writeD(buf, nr.getRewardItem1Count());
		writeD(buf, nr.getRewardItem2());
		writeD(buf, nr.getRewardItem2Count());
		writeD(buf, nr.getRewardItem3());
		writeD(buf, nr.getRewardItem3Count());
		writeD(buf, nr.getRewardItem4());
		writeD(buf, nr.getRewardItem4Count());
	}

}
