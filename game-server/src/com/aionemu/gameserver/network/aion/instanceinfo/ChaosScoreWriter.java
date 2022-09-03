package com.aionemu.gameserver.network.aion.instanceinfo;

import com.aionemu.gameserver.model.instance.instancescore.PvPArenaScore;

/**
 * @author xTz
 */
public class ChaosScoreWriter extends ArenaScoreWriter {

	public ChaosScoreWriter(PvPArenaScore reward, int ownerObjectId) {
		super(reward, ownerObjectId, false);
	}
}
