package com.aionemu.gameserver.network.aion.instanceinfo;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.model.instance.instancescore.DarkPoetaScore;

/**
 * @author xTz
 */
public class DarkPoetaScoreWriter extends InstanceScoreWriter<DarkPoetaScore> {

	public DarkPoetaScoreWriter(DarkPoetaScore reward) {
		super(reward);
	}

	@Override
	public void writeMe(ByteBuffer buf) {
		writeD(buf, instanceScore.getPoints());
		writeD(buf, instanceScore.getNpcKills());
		writeD(buf, instanceScore.getGatherCollections()); // gathers
		writeD(buf, instanceScore.getRank()); // 7 for none, 8 for F, 5 for D, 4 C, 3 B, 2 A, 1 S
	}

}
