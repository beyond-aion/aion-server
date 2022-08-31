package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.network.PacketWriteHelper;

/**
 * @author xTz
 */
public abstract class InstanceScoreInfo<T extends InstanceReward<?>> extends PacketWriteHelper {

	protected final T reward;

	public InstanceScoreInfo(T reward) {
		this.reward = reward;
	}

	public T getReward() {
		return reward;
	}

	@Override
	public void writeMe(ByteBuffer buf) {
	}

}
