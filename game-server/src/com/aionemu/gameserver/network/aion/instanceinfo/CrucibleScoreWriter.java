package com.aionemu.gameserver.network.aion.instanceinfo;

import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.model.instance.playerreward.CruciblePlayerReward;

import io.netty.buffer.ByteBuf;

/**
 * @author xTz
 */
public class CrucibleScoreWriter extends InstanceScoreWriter<InstanceScore<CruciblePlayerReward>> {

	public CrucibleScoreWriter(InstanceScore<CruciblePlayerReward> reward) {
		super(reward);
	}

	@Override
	public void writeMe(ByteBuf buf) {
		int playerCount = 0;
		for (CruciblePlayerReward playerReward : instanceScore.getPlayerRewards()) {
			writeD(buf, playerReward.getOwnerId()); // obj
			writeD(buf, playerReward.getPoints()); // points
			writeD(buf, instanceScore.getInstanceProgressionType().isEndProgress() ? 3 : 1);
			writeD(buf, playerReward.getInsignia());
			playerCount++;
		}
		if (playerCount < 6) {
			writeB(buf, new byte[16 * (6 - playerCount)]); // spaces
		}
	}
}
