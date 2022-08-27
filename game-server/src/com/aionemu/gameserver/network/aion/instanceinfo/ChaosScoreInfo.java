package com.aionemu.gameserver.network.aion.instanceinfo;

import com.aionemu.gameserver.model.instance.instancereward.PvPArenaReward;

/**
 * @author xTz
 */
public class ChaosScoreInfo extends ArenaScoreInfo {

	public ChaosScoreInfo(PvPArenaReward reward, int ownerObjectId) {
		super(reward, ownerObjectId, false);
	}
}
