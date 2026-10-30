package com.aionemu.gameserver.network.aion.instanceinfo;

import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.network.PacketWriteHelper;

import io.netty.buffer.ByteBuf;

/**
 * @author xTz
 */
public abstract class InstanceScoreWriter<T extends InstanceScore<?>> extends PacketWriteHelper {

	protected final T instanceScore;

	public InstanceScoreWriter(T instanceScore) {
		this.instanceScore = instanceScore;
	}

	public T getInstanceScore() {
		return instanceScore;
	}

	@Override
	public void writeMe(ByteBuf buf) {
	}

}
