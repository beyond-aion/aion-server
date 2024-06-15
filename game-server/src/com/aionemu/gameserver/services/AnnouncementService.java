package com.aionemu.gameserver.services;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dao.AnnouncementsDAO;
import com.aionemu.gameserver.model.Announcement;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Automatic Announcement System
 * 
 * @author Divinity
 */
public class AnnouncementService {

	private final Map<Integer, Future<?>> delays = new ConcurrentHashMap<>();
	private final Map<Integer, Announcement> announcements = new ConcurrentHashMap<>();

	private AnnouncementService() {
		reload();
	}

	public static AnnouncementService getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Reload the announcements system
	 */
	public void reload() {
		// Cancel all tasks
		for (Future<?> delay : delays.values())
			delay.cancel(true);
		delays.clear();
		announcements.clear();

		// And load again all announcements
		for (Announcement announcement : AnnouncementsDAO.loadAnnouncements())
			schedule(announcement);

		LoggerFactory.getLogger(AnnouncementService.class).info("Loaded " + announcements.size() + " announcements");
	}

	private void schedule(Announcement announce) {
		announcements.put(announce.getId(), announce);
		delays.put(announce.getId(), ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				String sender = announce.getFaction() == null ? "" : announce.getFaction() == Race.ELYOS ? "Elyos " : "Asmodian ";
				sender += "Announcement";
				String msg = announce.getChatType() == ChatType.SHOUT || announce.getChatType() == ChatType.GROUP_LEADER ? "" : sender + ": ";
				msg += announce.getAnnounce();
				SM_MESSAGE message = new SM_MESSAGE(1, sender, msg, announce.getChatType());
				PacketSendUtility.broadcastToWorld(message, player -> player.getOppositeRace() != announce.getFaction());
			}
		}, announce.getDelay() * 1000, announce.getDelay() * 1000));
	}

	public boolean addAnnouncement(String message, String faction, String chatType, int delay) {
		int id = AnnouncementsDAO.addAnnouncement(message, faction, chatType, delay);
		if (id == -1)
			return false;
		schedule(new Announcement(id, message, faction, chatType, delay));
		return true;
	}

	public boolean delAnnouncement(int id) {
		if (announcements.remove(id) == null || !AnnouncementsDAO.delAnnouncement(id))
			return false;
		Future<?> delay = delays.remove(id);
		if (delay != null)
			delay.cancel(false);
		return true;
	}

	public Collection<Announcement> getAnnouncements() {
		return announcements.values();
	}

	private static class SingletonHolder {

		protected static final AnnouncementService instance = new AnnouncementService();
	}
}
