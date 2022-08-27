package com.aionemu.gameserver.network.aion.instanceinfo;

import com.aionemu.gameserver.model.instance.instancereward.PvPArenaReward;

/**
 * @author xTz
 */
public class DisciplineScoreInfo extends ArenaScoreInfo {

	public DisciplineScoreInfo(PvPArenaReward reward, int ownerObjectId) {
		super(reward, ownerObjectId, true);
	}
}
