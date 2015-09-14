package com.aionemu.gameserver.dao;

import java.sql.Timestamp;

import com.aionemu.commons.database.dao.DAO;

/**
 * @author ViAl
 */
public abstract class InGameShopLogDAO implements DAO {

	public abstract void log(String transactionType, Timestamp transactionDate, String payerName, String payerAccountName, String receiverName,
		int itemId, long itemCount, long itemPrice);

	@Override
	public String getClassName() {
		return InGameShopLogDAO.class.getName();
	}

}
