package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.instanceinfo.ArenaScoreInfo;
import com.aionemu.gameserver.network.aion.instanceinfo.InstanceScoreInfo;

/**
 * @author Dns, ginho1, nrg, xTz
 */
public class SM_INSTANCE_SCORE extends AionServerPacket {

	private final int mapId;
	private final int instanceTime;
	private final InstanceScoreInfo<?> isi;

	public SM_INSTANCE_SCORE(int mapId, ArenaScoreInfo arenaScoreInfo) {
		this(mapId, arenaScoreInfo, arenaScoreInfo.getReward().getTime());
	}

	public SM_INSTANCE_SCORE(int mapId, InstanceScoreInfo<?> instanceScoreInfo) {
		this(mapId, instanceScoreInfo, 0);
	}

	public SM_INSTANCE_SCORE(int mapId, InstanceScoreInfo<?> instanceScoreInfo, int instanceTime) {
		this.mapId = mapId;
		this.instanceTime = instanceTime;
		this.isi = instanceScoreInfo;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(mapId);
		writeD(instanceTime);
		writeD(isi.getReward().getInstanceProgressionType().getId());
		isi.writeMe(buf);
	}
}
