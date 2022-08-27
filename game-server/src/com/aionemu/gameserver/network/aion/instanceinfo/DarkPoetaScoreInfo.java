package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.instance.instancereward.DarkPoetaReward;

/**
 * @author xTz
 */
public class DarkPoetaScoreInfo extends InstanceScoreInfo<DarkPoetaReward> {

	public DarkPoetaScoreInfo(DarkPoetaReward reward) {
		super(reward);
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeD(buf, reward.getPoints());
		writeD(buf, reward.getNpcKills());
		writeD(buf, reward.getGatherCollections()); // gathers
		writeD(buf, reward.getRank()); // 7 for none, 8 for F, 5 for D, 4 C, 3 B, 2 A, 1 S
	}

}
