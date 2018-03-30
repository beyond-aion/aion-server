package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.instance.InstanceProgressionType;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.instanceinfo.InstanceScoreInfo;

/**
 * @author Dns, ginho1, nrg, xTz
 * @modified Neon
 */
public class SM_INSTANCE_SCORE extends AionServerPacket {

	private int mapId;
	private int instanceTime;
	private InstanceProgressionType instanceScoreType;
	private InstanceScoreInfo isi;

	public SM_INSTANCE_SCORE(InstanceScoreInfo isi, InstanceReward<?> instanceReward) {
		this(isi, instanceReward, null, 0);
	}

	public SM_INSTANCE_SCORE(InstanceScoreInfo isi, InstanceReward<?> instanceReward, int instanceTime) {
		this(isi, instanceReward, null, instanceTime);
	}

	public SM_INSTANCE_SCORE(InstanceScoreInfo isi, InstanceReward<?> instanceReward, InstanceProgressionType instanceScoreType) {
		this(isi, instanceReward, instanceScoreType, 0);
	}

	private SM_INSTANCE_SCORE(InstanceScoreInfo isi, InstanceReward<?> instanceReward, InstanceProgressionType instanceScoreType, int instanceTime) {
		this.instanceScoreType = instanceScoreType != null ? instanceScoreType : instanceReward.getInstanceProgressionType();
		this.mapId = instanceReward.getMapId();
		this.instanceTime = instanceTime;
		this.isi = isi;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(mapId);
		writeD(instanceTime);
		writeD(instanceScoreType.getId());
		if (isi != null) {
			isi.writeMe(buf);
		}
	}
}
