package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;

/**
 * @author ViAl
 */
public class InGameShopLogDAO {

	private static final Logger log = LoggerFactory.getLogger(InGameShopLogDAO.class);

	private static final String INSERT_QUERY = "INSERT INTO `ingameshop_log` (`transaction_type`, `transaction_date`, `payer_name`, `payer_account_name`, `receiver_name`, `item_id`, `item_count`, `item_price`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	public static void log(String transactionType, Timestamp transactionDate, String payerName, String payerAccountName, String receiverName, int itemId,
		long itemCount, long itemPrice) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
			stmt.setString(1, transactionType);
			stmt.setTimestamp(2, transactionDate);
			stmt.setString(3, payerName);
			stmt.setString(4, payerAccountName);
			stmt.setString(5, receiverName);
			stmt.setInt(6, itemId);
			stmt.setLong(7, itemCount);
			stmt.setLong(8, itemPrice);
			stmt.executeUpdate();
		} catch (SQLException e) {
			log.error("Could not insert ingameshop log", e);
		}
	}

}
