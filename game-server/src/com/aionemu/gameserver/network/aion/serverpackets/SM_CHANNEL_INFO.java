package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.configs.main.WorldConfig;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author ATracer
 */
public class SM_CHANNEL_INFO extends AionServerPacket {

	int instanceCount = 0;
	int currentChannel = 0;

	/**
	 * @param position
	 */
	public SM_CHANNEL_INFO(WorldPosition position) {
		if (!position.isSpawned()) {
			instanceCount = 1;
			currentChannel = 1;
			return;
		}
		WorldMapTemplate template = position.getWorldMapInstance().getTemplate();
		if (position.getWorldMapInstance().isBeginnerInstance()) {
			this.instanceCount = template.getBeginnerTwinCount();
			if (WorldConfig.WORLD_EMULATE_FASTTRACK)
				this.instanceCount += template.getTwinCount();
			this.currentChannel = position.getInstanceId() - 1;
		}
		else {
			this.instanceCount = template.getTwinCount();
			if (WorldConfig.WORLD_EMULATE_FASTTRACK)
				this.instanceCount += template.getBeginnerTwinCount();
			this.currentChannel = position.getInstanceId() - 1;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeD(currentChannel);
		writeD(instanceCount);
	}
}
