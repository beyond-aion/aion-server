package com.aionemu.loginserver.controller;

import java.sql.Timestamp;
import java.util.Map;

import javolution.util.FastMap;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.loginserver.dao.BannedHddDAO;


/**
 * @author ViAl
 *
 */
public class BannedHDDController {
	
	private static BannedHDDController instance = new BannedHDDController();
	private BannedHddDAO dao = DAOManager.getDAO(BannedHddDAO.class);
	
	private Map<String, Timestamp> bannedList = new FastMap<String, Timestamp>();

	public static BannedHDDController getInstance() {
		return instance;
	}

	public BannedHDDController() {
		bannedList = dao.load();
	}

	public void unban(String serial) {
		if(bannedList.containsKey(serial)) {
			bannedList.remove(serial);
			dao.remove(serial);
		}
	}

	public void ban(String serial, long time) {
		Timestamp banTime = new Timestamp(time);
		this.bannedList.put(serial, banTime);
		this.dao.update(serial, banTime);
	}

	public final Map<String, Timestamp> getMap() {
		return this.bannedList;
	}
	
	

}
