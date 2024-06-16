package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.templates.survey.SurveyItem;

/**
 * @author KID
 */
public class SurveyControllerDAO {

	private static final Logger log = LoggerFactory.getLogger(SurveyControllerDAO.class);

	public static final String UPDATE_QUERY = "UPDATE `surveys` SET `used`=?, used_time=NOW() WHERE `unique_id`=?";
	public static final String SELECT_QUERY = "SELECT * FROM `surveys` WHERE `used`=?";

	public static List<SurveyItem> getAllUnused() {
		List<SurveyItem> list = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, 0);
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					SurveyItem item = new SurveyItem();
					item.uniqueId = rset.getInt("unique_id");
					item.ownerId = rset.getInt("owner_id");
					item.itemId = rset.getInt("item_id");
					item.count = rset.getLong("item_count");
					item.html = rset.getString("html_text");
					item.radio = rset.getString("html_radio");
					list.add(item);
				}
			}
		} catch (Exception e) {
			log.error("Could not load new surveys", e);
		}
		return list;
	}

	public static boolean useItem(int id) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
			stmt.setInt(1, 1);
			stmt.setInt(2, id);
			stmt.execute();
		} catch (Exception e) {
			log.error("Could not set used state for survey " + id, e);
			return false;
		}
		return true;
	}

}
