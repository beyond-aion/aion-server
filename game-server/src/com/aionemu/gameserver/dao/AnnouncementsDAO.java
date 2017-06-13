package com.aionemu.gameserver.dao;

import java.util.List;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.Announcement;

/**
 * DAO that manages Announcements
 * 
 * @author Divinity
 */
public abstract class AnnouncementsDAO implements DAO {

	public abstract List<Announcement> loadAnnouncements();

	/**
	 * @return The DB ID of the added announcement or -1 on error.
	 */
	public abstract int addAnnouncement(String message, String faction, String chatType, int delay);

	public abstract boolean delAnnouncement(int id);

	/**
	 * Returns class name that will be uses as unique identifier for all DAO classes
	 * 
	 * @return class name
	 */
	@Override
	public final String getClassName() {
		return AnnouncementsDAO.class.getName();
	}
}
