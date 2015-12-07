package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;

/**
 * @author Estrayl
 */
public abstract class BonusPackDAO implements DAO {

	public abstract int loadReceivingPlayer(int accountId);

	public abstract boolean storeReceivingPlayer(int accountId, int playerId);

	@Override
	public String getClassName() {
		return BonusPackDAO.class.getName();
	}
}
