package com.aionemu.gameserver.network.aion.clientpackets;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Set;

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
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_ACCOUNT_CONNECTION_INFO;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_CHANGE_ALLOWED_HDD_SERIAL;
import com.aionemu.gameserver.services.ban.HDDBanService;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * In this packet client is sending connection and system information (IPs, MAC Address and HDD serial).
 * 
 * @author -Nemesiss-, KID, ViAl, Neon
 */
public class CM_MAC_ADDRESS extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_MAC_ADDRESS.class);
	private String macAddress, hddSerial;

	public CM_MAC_ADDRESS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		readC(); // unk
		int routeSteps = readUH();
		for (int i = 0; i < routeSteps; i++)
			readD(); // ip traceroute, see -> System.out.println(NetworkUtils.intToIpString(readD()))
		macAddress = readS();
		hddSerial = fixHddSerial(readS());
		readD(); // local IP, see -> System.out.println(NetworkUtils.intToIpString(readD()))
	}

	@Override
	protected void runImpl() {
		AionConnection con = getConnection();
		if (BannedMacManager.getInstance().isBanned(macAddress)) {
			con.close();
			log.info("[MAC_AUDIT] " + con.getAccount() + ", MAC " + macAddress + " (" + con.getIP() + ") was kicked due to mac ban");
		} else if (HDDBanService.getInstance().isBanned(hddSerial)) {
			con.close();
			log.info("[MAC_AUDIT] " + con.getAccount() + ", HDD Serial " + hddSerial + " (" + con.getIP() + ") was kicked due to hdd serial ban");
		} else {
			boolean firstPacket = con.getMacAddress() == null;
			con.setMacAddress(macAddress);
			con.setHddSerial(hddSerial);

			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					Account account = getConnection().getAccount();
					if (account == null)
						return;
					if (firstPacket) // don't log re-entering from server selection
						LoginServer.getInstance()
							.sendPacket(new SM_ACCOUNT_CONNECTION_INFO(account.getId(), System.currentTimeMillis(), con.getIP(), macAddress, hddSerial));
					if (account.getAllowedHddSerial() == null) {
						if (SecurityConfig.HDD_SERIAL_LOCK_NEW_ACCOUNTS && !hddSerial.isEmpty()) {
							account.setAllowedHddSerial(hddSerial);
							LoginServer.getInstance().sendPacket(new SM_CHANGE_ALLOWED_HDD_SERIAL(account));
						}
					} else if (!account.getAllowedHddSerial().equalsIgnoreCase(hddSerial)) {
						if (SecurityConfig.HDD_SERIAL_HACKED_ACCOUNTS_KICK) {
							con.close(SM_SYSTEM_MESSAGE.STR_L2AUTH_S_SYSTEM_ERROR());
							log.info(
								"[MAC_AUDIT] " + account + ",  HDD Serial " + hddSerial + " (" + con.getIP() + ") was kicked due to allowed hdd serial mismatch");
						} else {
							account.setHacked(true);
						}
					}
				}
			}, 5 * 1000);
		}
	}

	private static String fixHddSerial(String hddSerial) {
		if (!hddSerial.matches("[0-9a-zA-Z _-]+")) { // most likely not a serial number string, so we need to convert it
			byte[] bytes = hddSerial.getBytes();
			if (bytes.length % Integer.BYTES == 0) { // check if the length fits exactly one or more ints
				StringBuilder sb = new StringBuilder();
				for (IntBuffer intBuffer = ByteBuffer.wrap(bytes).asIntBuffer(); intBuffer.hasRemaining();)
					sb.append(intBuffer.get()); // concatenate all ints of this buffer to build the serial number
				return sb.toString();
			}
		} else if (hddSerial.matches("^[a-zA-Z0-9] [a-zA-Z0-9_-].*|.*[a-zA-Z0-9_-] [a-zA-Z0-9]$")) { // check if second or penultimate char is a space
			return hddSerial.replaceAll("(.)(.)", "$2$1").trim(); // for some reason some clients send switched chars
		}
		return hddSerial.trim();
	}
}
