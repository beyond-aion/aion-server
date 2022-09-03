package com.aionemu.gameserver.network.aion.instanceinfo;

import com.aionemu.gameserver.model.instance.instancescore.PvPArenaScore;

/**
 * @author xTz
 */
public class DisciplineScoreWriter extends ArenaScoreWriter {

	public DisciplineScoreWriter(PvPArenaScore reward, int ownerObjectId) {
		super(reward, ownerObjectId, true);
	}
}
