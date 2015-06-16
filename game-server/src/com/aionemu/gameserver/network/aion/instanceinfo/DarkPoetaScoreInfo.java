package com.aionemu.gameserver.network.aion.instanceinfo;

import com.aionemu.gameserver.model.instance.instancereward.DarkPoetaReward;
import java.nio.ByteBuffer;

/**
 *
 * @author xTz
 */
public class DarkPoetaScoreInfo extends InstanceScoreInfo {

    private final DarkPoetaReward dpr;

    public DarkPoetaScoreInfo(DarkPoetaReward dpr) {
        this.dpr = dpr;
    }

    @Override
    public void writeMe(ByteBuffer buf) {
        writeD(buf, dpr.getPoints());
        writeD(buf, dpr.getNpcKills());
        writeD(buf, dpr.getGatherCollections()); // gathers
        writeD(buf, dpr.getRank()); // 7 for none, 8 for F, 5 for D, 4 C, 3 B, 2 A, 1 S
    }

}
