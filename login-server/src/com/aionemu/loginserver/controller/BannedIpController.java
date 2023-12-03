package com.aionemu.loginserver.controller;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.NetworkUtils;
import com.aionemu.loginserver.dao.BannedIpDAO;
import com.aionemu.loginserver.model.BannedIP;

/**
 * Class that controlls all ip banning activity
 * 
 * @author SoulKeeper
 */
public class BannedIpController {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(BannedIpController.class);

	/**
	 * List of banned ip adresses
	 */
	private static Set<BannedIP> banList;

	public static void start() {
		clean();
		load();
	}

	private static void clean() {
		BannedIpDAO.cleanExpiredBans();
	}

	/**
	 * Loads list of banned ips
	 */
	public static void load() {
		reload();
	}

	/**
	 * Loads list of banned ips
	 */
	public static void reload() {
		// we are not going to make ip ban every minute, so it's ok to simplify a concurrent code a bit
		banList = BannedIpDAO.getAllBans();
		log.info("BannedIpController loaded " + banList.size() + " IP bans.");
	}

	/**
	 * Checks if ip (or mask) is banned
	 * 
	 * @param ip
	 *          ip address to check for ban
	 * @return is it banned or not
	 */
	public static boolean isBanned(String ip) {
		for (BannedIP ipBan : banList) {
			if (ipBan.isActive() && NetworkUtils.checkIPMatching(ipBan.getMask(), ip))
				return true;
		}
		return false;
	}

	/**
	 * Bans ip or mask for infinite period of time
	 * 
	 * @param ip
	 *          ip to ban
	 * @return was ip banned or not
	 */
	public static boolean banIp(String ip) {
		return banIp(ip, null);
	}

	/**
	 * Bans ip (or mask)
	 * 
	 * @param ip
	 *          ip to ban
	 * @param expireTime
	 *          ban expiration time, null = never expires
	 * @return was ip banned or not
	 */
	public static boolean banIp(String ip, Timestamp expireTime) {
		BannedIP ipBan = new BannedIP();
		ipBan.setMask(ip);
		ipBan.setTimeEnd(expireTime);
		return banList.add(ipBan) && BannedIpDAO.insert(ipBan);
	}

	/**
	 * Adds or updates ip ban. Changes are reflected in DB
	 * 
	 * @param ipBan
	 *          banned ip to add or change
	 * @return was it updated or not
	 */
	public static boolean addOrUpdateBan(BannedIP ipBan) {
		if (ipBan.getId() == null) {
			if (BannedIpDAO.insert(ipBan)) {
				banList.add(ipBan);
				return true;
			}
			return false;
		}
		return BannedIpDAO.update(ipBan);
	}

	/**
	 * Removes ip ban.
	 * 
	 * @param ip
	 *          ip to unban
	 * @return returns true if ip was successfully unbanned
	 */
	public static boolean unbanIp(String ip) {
		Iterator<BannedIP> it = banList.iterator();
		while (it.hasNext()) {
			BannedIP ipBan = it.next();
			if (ipBan.getMask().equals(ip)) {
				if (BannedIpDAO.remove(ipBan)) {
					it.remove();
					return true;
				}
				break;
			}
		}
		return false;
	}
}
