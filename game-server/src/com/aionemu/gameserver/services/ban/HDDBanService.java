package com.aionemu.gameserver.services.ban;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_HDDBAN_CONTROL;

/**
 * @author ViAl
 */
public class HDDBanService {

	/**
	 * HDD serial - ban time
	 */
	private Map<String, Timestamp> bannedSerials = new HashMap<>();

	public static HDDBanService getInstance() {
		return SingletonHolder.instance;
	}

	private static final class SingletonHolder {

		protected static final HDDBanService instance = new HDDBanService();
	}

	public void addBan(String serial, Timestamp banTime) {
		bannedSerials.put(serial, banTime);
		LoginServer.getInstance().sendPacket(new SM_HDDBAN_CONTROL(BanAction.BAN, serial, banTime.getTime()));
	}

	public void removeBan(String serial) {
		this.bannedSerials.remove(serial);
		LoginServer.getInstance().sendPacket(new SM_HDDBAN_CONTROL(BanAction.UNBAN, serial, 0));
	}

	public void loadBan(String serial, long banTime) {
		this.bannedSerials.put(serial, new Timestamp(banTime));
	}

	public boolean isBanned(String serial) {
		if (!this.bannedSerials.containsKey(serial))
			return false;
		Timestamp banTime = bannedSerials.get(serial);
		if (banTime.getTime() > System.currentTimeMillis())
			return true;
		else
			return false;
	}
}
