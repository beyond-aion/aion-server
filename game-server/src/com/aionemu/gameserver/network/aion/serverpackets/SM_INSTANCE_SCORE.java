package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.instanceinfo.ArenaScoreWriter;
import com.aionemu.gameserver.network.aion.instanceinfo.InstanceScoreWriter;

/**
 * @author Dns, ginho1, nrg, xTz
 */
public class SM_INSTANCE_SCORE extends AionServerPacket {

	private final int mapId;
	private final int instanceTime;
	private final InstanceScoreWriter<?> instanceScoreWriter;

	public SM_INSTANCE_SCORE(int mapId, ArenaScoreWriter arenaScoreInfo) {
		this(mapId, arenaScoreInfo, arenaScoreInfo.getInstanceScore().getTime());
	}

	public SM_INSTANCE_SCORE(int mapId, InstanceScoreWriter<?> instanceScoreWriter) {
		this(mapId, instanceScoreWriter, 0);
	}

	public SM_INSTANCE_SCORE(int mapId, InstanceScoreWriter<?> instanceScoreWriter, int instanceTime) {
		this.mapId = mapId;
		this.instanceTime = instanceTime;
		this.instanceScoreWriter = instanceScoreWriter;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(mapId);
		writeD(instanceTime);
		writeD(instanceScoreWriter.getInstanceScore().getInstanceProgressionType().getId());
		instanceScoreWriter.writeMe(buf);
	}
}
