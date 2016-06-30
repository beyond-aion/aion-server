package com.aionemu.gameserver.dao;

import java.util.List;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.templates.rewards.RewardEntryItem;

/**
 * @author KID
 */
public abstract class RewardServiceDAO implements DAO {

	@Override
	public final String getClassName() {
		return RewardServiceDAO.class.getName();
	}

	/**
	 * @param playerId
	 * @return Rewards that have not yet been added to the player.
	 */
	public abstract List<RewardEntryItem> loadUnreceived(int playerId);

	/**
	 * Sets all given reward entries as received, so the player cannot receive it again.
	 * 
	 * @param entryIds
	 * @param timeReceived
	 */
	public abstract void storeReceived(List<Integer> entryIds, long timeReceived);
}
