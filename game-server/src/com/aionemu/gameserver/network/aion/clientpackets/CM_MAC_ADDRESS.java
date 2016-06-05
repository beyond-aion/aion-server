package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.SecurityConfig;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.network.BannedMacManager;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_ACCOUNT_LOGIN_LOG;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_CHANGE_ALLOWED_HDD_SERIAL;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_HDD_SERIAL;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_MAC;
import com.aionemu.gameserver.services.ban.HDDBanService;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * In this packet client is sending Mac Address - haha.
 * 
 * @author -Nemesiss-, KID, ViAl
 */
public class CM_MAC_ADDRESS extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_MAC_ADDRESS.class);
	/**
	 * Mac Addres send by client in the same format as: ipconfig /all [ie: xx-xx-xx-xx-xx-xx]
	 */
	private String macAddress;
	/**
	 * Client HDD serial
	 */
	private String hddSerial;

	/**
	 * Constructs new instance of <tt>CM_MAC_ADDRESS </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_MAC_ADDRESS(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		readC();
		short counter = (short) readH();
		for (short i = 0; i < counter; i++)
			readD();
		macAddress = readS();
		hddSerial = readS();
	}

	@Override
	protected void runImpl() {
		AionConnection con = getConnection();
		if (BannedMacManager.getInstance().isBanned(macAddress)) {
			// TODO some information packets
			con.close();
			log.info("[MAC_AUDIT] " + macAddress + " (" + con.getIP() + ") was kicked due to mac ban");
		} else if (HDDBanService.getInstance().isBanned(hddSerial)) {
			con.close();
			log.info("[MAC_AUDIT] Account:" + con.getAccount().getName() + ", HDD Serial " + hddSerial + " (" + con.getIP()
				+ ") was kicked due to hdd serial ban");
		} else {
			con.setMacAddress(macAddress);
			con.setHddSerial(hddSerial);
		}
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (getConnection() == null)
					return;
				Account account = getConnection().getAccount();
				LoginServer.getInstance().sendPacket(new SM_MAC(account.getId(), macAddress));
				LoginServer.getInstance().sendPacket(new SM_HDD_SERIAL(account.getId(), hddSerial));
				LoginServer.getInstance()
					.sendPacket(new SM_ACCOUNT_LOGIN_LOG(account.getId(), System.currentTimeMillis(), getConnection().getIP(), macAddress, hddSerial));
				if (account.getAllowedHddSerial() == null) {
					if (SecurityConfig.HDD_SERIAL_LOCK_NEW_ACCOUNTS && !hddSerial.isEmpty()) {
						account.setAllowedHddSerial(hddSerial);
						LoginServer.getInstance().sendPacket(new SM_CHANGE_ALLOWED_HDD_SERIAL(account));
					}
				} else if (!account.getAllowedHddSerial().equalsIgnoreCase(hddSerial)) {
					if (SecurityConfig.HDD_SERIAL_HACKED_ACCOUNTS_KICK) {
						getConnection().sendPacket(SM_SYSTEM_MESSAGE.STR_L2AUTH_S_SYSTEM_ERROR());
						getConnection().close();
						log.info("[MAC_AUDIT] " + account + ",  HDD Serial " + hddSerial + " (" + getConnection().getIP()
							+ ") was kicked due to allowed hdd serial mismatch");
					} else {
						account.setHacked(true);
					}
				}
			}
		}, 10 * 1000);
	}
}
