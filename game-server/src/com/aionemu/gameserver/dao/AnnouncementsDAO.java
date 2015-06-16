package com.aionemu.gameserver.dao;

import java.util.Set;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.Announcement;

/**
 * DAO that manages Announcements
 * 
 * @author Divinity
 */
public abstract class AnnouncementsDAO implements DAO {

	public abstract Set<Announcement> getAnnouncements();

	public abstract void addAnnouncement(final Announcement announce);

	public abstract boolean delAnnouncement(final int idAnnounce);

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
