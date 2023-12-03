package com.aionemu.loginserver.controller;

import java.sql.Timestamp;
import java.util.Map;

import com.aionemu.loginserver.dao.BannedHddDAO;

/**
 * @author ViAl
 */
public class BannedHDDController {

	private static final BannedHDDController instance = new BannedHDDController();

	private final Map<String, Timestamp> bannedList;

	public static BannedHDDController getInstance() {
		return instance;
	}

	private BannedHDDController() {
		bannedList = BannedHddDAO.load();
	}

	public void unban(String serial) {
		if (bannedList.containsKey(serial)) {
			bannedList.remove(serial);
			BannedHddDAO.remove(serial);
		}
	}

	public void ban(String serial, long time) {
		Timestamp banTime = new Timestamp(time);
		bannedList.put(serial, banTime);
		BannedHddDAO.update(serial, banTime);
	}

	public final Map<String, Timestamp> getMap() {
		return bannedList;
	}

}
