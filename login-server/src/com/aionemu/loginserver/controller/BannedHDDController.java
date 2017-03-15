package com.aionemu.loginserver.controller;

import java.sql.Timestamp;
import java.util.Map;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.loginserver.dao.BannedHddDAO;

/**
 * @author ViAl
 */
public class BannedHDDController {

	private static BannedHDDController instance = new BannedHDDController();
	private BannedHddDAO dao = DAOManager.getDAO(BannedHddDAO.class);

	private Map<String, Timestamp> bannedList;

	public static BannedHDDController getInstance() {
		return instance;
	}

	private BannedHDDController() {
		bannedList = dao.load();
	}

	public void unban(String serial) {
		if (bannedList.containsKey(serial)) {
			bannedList.remove(serial);
			dao.remove(serial);
		}
	}

	public void ban(String serial, long time) {
		Timestamp banTime = new Timestamp(time);
		bannedList.put(serial, banTime);
		dao.update(serial, banTime);
	}

	public final Map<String, Timestamp> getMap() {
		return bannedList;
	}

}
