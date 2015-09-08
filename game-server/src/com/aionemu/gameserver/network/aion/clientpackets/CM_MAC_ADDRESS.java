package com.aionemu.gameserver.network.aion.clientpackets;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.network.BannedMacManager;
import com.aionemu.gameserver.network.aion.AionClientPacket;
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
	 * Mac Addres send by client in the same format as: ipconfig /all [ie:
	 * xx-xx-xx-xx-xx-xx]
	 */
	private String	macAddress;
	/**
	 * Client HDD serial
	 */
	private String	hddSerial;

	/**
	 * Constructs new instance of <tt>CM_MAC_ADDRESS </tt> packet
	 * 
	 * @param opcode
	 */
	public CM_MAC_ADDRESS(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		readC();
		short counter = (short)readH();
		for(short i = 0; i < counter; i++)
			readD();
		macAddress = readS();
		hddSerial = readS();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {		
		if(BannedMacManager.getInstance().isBanned(macAddress)) {
			//TODO some information packets
			this.getConnection().close();
			log.info("[MAC_AUDIT] "+macAddress+" ("+this.getConnection().getIP()+") was kicked due to mac ban");
		}
		else if(HDDBanService.getInstance().isBanned(hddSerial)) {
			this.getConnection().close();
			log.info("[MAC_AUDIT] Account:"+this.getConnection().getAccount().getName()+", HDD Serial "+hddSerial+" ("+this.getConnection().getIP()+") was kicked due to hdd serial ban");
		}
		else {
			this.getConnection().setMacAddress(macAddress);
			this.getConnection().setHddSerial(hddSerial);
		}
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				Account account = getConnection().getAccount();
				LoginServer.getInstance().sendPacket(new SM_MAC(account.getId(), macAddress));
				LoginServer.getInstance().sendPacket(new SM_HDD_SERIAL(account.getId(), hddSerial));
				if(LoggingConfig.LOG_ACCOUNTS_LOGIN)
					LoginServer.getInstance().sendPacket(new SM_ACCOUNT_LOGIN_LOG(account.getId(), NetworkConfig.GAMESERVER_ID, System.currentTimeMillis(), getConnection().getIP(), macAddress, hddSerial));
				//TEMP FIX
				if(account.getAllowedHddSerial().isEmpty() && AntiHackConfig.HDD_SERIAL_LOCK_NEW_ACCOUNTS) {
					account.setAllowedHddSerial(hddSerial);
					LoginServer.getInstance().sendPacket(new SM_CHANGE_ALLOWED_HDD_SERIAL(account.getId(), hddSerial));
				}
				if(!HDDBanService.getInstance().isAllowed(account.getAllowedHddSerial(), hddSerial)) {
					if(AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_KICK) {
						getConnection().sendPacket(SM_SYSTEM_MESSAGE.STR_L2AUTH_S_SYSTEM_ERROR);
						getConnection().close();
						log.info("[MAC_AUDIT] Account:"+account.getName()+",  HDD Serial "+hddSerial+" ("+getConnection().getIP()+") was kicked due to allowed hdd serial mismatch");
					}
					else {
						account.setHacked(true);
					}
				}
			}
		}, 5* 1000);
	}
}
