package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.instance.InstanceScoreType;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.instanceinfo.InstanceScoreInfo;

/**
 * @author Dns, ginho1, nrg, xTz
 */
public class SM_INSTANCE_SCORE extends AionServerPacket {

	private int mapId;
	private int instanceTime;
	private InstanceScoreType instanceScoreType;
    private InstanceScoreInfo isi;

    public SM_INSTANCE_SCORE(InstanceScoreInfo isi, InstanceReward instanceReward) {
        this(isi, instanceReward, 0);
    }
 
    public SM_INSTANCE_SCORE(InstanceScoreInfo isi, InstanceReward instanceReward, InstanceScoreType instanceScoreType) {
        this(isi, instanceReward, 0);
        this.instanceScoreType = instanceScoreType;
    }

    public SM_INSTANCE_SCORE(InstanceScoreInfo isi, InstanceReward instanceReward, int instanceTime) {
        instanceScoreType = instanceReward.getInstanceScoreType();
        this.mapId = instanceReward.getMapId();
		this.instanceTime = instanceTime;
        this.isi = isi;
    }

	/**
	 * {@inheritDoc}
	 */
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