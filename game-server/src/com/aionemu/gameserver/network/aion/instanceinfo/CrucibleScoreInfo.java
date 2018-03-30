package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.model.instance.playerreward.CruciblePlayerReward;

/**
 * @author xTz
 */
public class CrucibleScoreInfo extends InstanceScoreInfo {

	private final InstanceReward<CruciblePlayerReward> instanceReward;
	private final InstanceProgressionType instanceScoreType;

	public CrucibleScoreInfo(InstanceReward<CruciblePlayerReward> instanceReward) {
		this(instanceReward, instanceReward.getInstanceProgressionType());
	}

	public CrucibleScoreInfo(InstanceReward<CruciblePlayerReward> instanceReward, InstanceProgressionType instanceScoreType) {
		this.instanceScoreType = instanceScoreType;
		this.instanceReward = instanceReward;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		int playerCount = 0;
		for (CruciblePlayerReward playerReward : instanceReward.getInstanceRewards()) {
			writeD(buf, playerReward.getOwnerId()); // obj
			writeD(buf, playerReward.getPoints()); // points
			writeD(buf, instanceScoreType.isEndProgress() ? 3 : 1);
			writeD(buf, playerReward.getInsignia());
			playerCount++;
		}
		if (playerCount < 6) {
			writeB(buf, new byte[16 * (6 - playerCount)]); // spaces
		}
	}
}
