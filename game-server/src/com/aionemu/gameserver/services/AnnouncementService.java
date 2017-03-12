package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.AnnouncementsDAO;
import com.aionemu.gameserver.model.Announcement;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * Automatic Announcement System
 * 
 * @author Divinity
 */
public class AnnouncementService {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(AnnouncementService.class);

	private Set<Announcement> announcements = new HashSet<>();
	private List<Future<?>> delays = new ArrayList<>();

	private AnnouncementService() {
		load();
	}

	public static final AnnouncementService getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Reload the announcements system
	 */
	public void reload() {
		// Cancel all tasks
		if (!delays.isEmpty()) {
			for (Future<?> delay : delays)
				delay.cancel(true);
			delays.clear();
		}
		// Clear all announcements
		announcements.clear();

		// And load again all announcements
		load();
	}

	/**
	 * Load the announcements system
	 */
	private void load() {
		announcements.clear();
		announcements.addAll(getDAO().getAnnouncements());

		for (Announcement announceMent : announcements) {
			delays.add(ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

				private Announcement announce = announceMent;

				@Override
				public void run() {
					final Iterator<Player> iter = World.getInstance().getPlayersIterator();
					while (iter.hasNext()) {
						Player player = iter.next();

						if (announce.getFaction().equalsIgnoreCase("ALL"))
							if (announce.getChatType() == ChatType.SHOUT || announce.getChatType() == ChatType.GROUP_LEADER)
								PacketSendUtility.sendPacket(player, new SM_MESSAGE(1, "Announcement", announce.getAnnounce(), announce.getChatType()));
							else
								PacketSendUtility.sendPacket(player,
									new SM_MESSAGE(1, "Announcement", "Announcement: " + announce.getAnnounce(), announce.getChatType()));
						else if (announce.getFactionEnum() == player.getRace())
							if (announce.getChatType() == ChatType.SHOUT || announce.getChatType() == ChatType.GROUP_LEADER)
								PacketSendUtility.sendPacket(player,
									new SM_MESSAGE(1, (announce.getFaction().equalsIgnoreCase("ELYOS") ? "Elyos" : "Asmodian") + " Announcement",
										announce.getAnnounce(), announce.getChatType()));
							else
								PacketSendUtility.sendPacket(player,
									new SM_MESSAGE(1, (announce.getFaction().equalsIgnoreCase("ELYOS") ? "Elyos" : "Asmodian") + " Announcement",
										(announce.getFaction().equalsIgnoreCase("ELYOS") ? "Elyos" : "Asmodian") + " Announcement: " + announce.getAnnounce(),
										announce.getChatType()));
					}
				}
			}, announceMent.getDelay() * 1000, announceMent.getDelay() * 1000));
		}

		log.info("Loaded " + announcements.size() + " announcements");
	}

	public void addAnnouncement(Announcement announce) {
		getDAO().addAnnouncement(announce);
	}

	public boolean delAnnouncement(final int idAnnounce) {
		return getDAO().delAnnouncement(idAnnounce);
	}

	public Set<Announcement> getAnnouncements() {
		return getDAO().getAnnouncements();
	}

	/**
	 * Retuns {@link com.aionemu.loginserver.dao.AnnouncementDAO} , just a shortcut
	 * 
	 * @return {@link com.aionemu.loginserver.dao.AnnouncementDAO}
	 */
	private AnnouncementsDAO getDAO() {
		return DAOManager.getDAO(AnnouncementsDAO.class);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final AnnouncementService instance = new AnnouncementService();
	}
}
