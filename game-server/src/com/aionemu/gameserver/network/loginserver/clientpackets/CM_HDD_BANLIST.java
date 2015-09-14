package com.aionemu.gameserver.network.loginserver.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.network.loginserver.LsClientPacket;
import com.aionemu.gameserver.services.ban.HDDBanService;

/**
 * @author ViAl
 */
public class CM_HDD_BANLIST extends LsClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_HDD_BANLIST.class);
	private int count;

	public CM_HDD_BANLIST(int opCode) {
		super(opCode);
	}

	@Override
	protected void readImpl() {
		count = readD();
		for (int a = 0; a < count; a++) {
			HDDBanService.getInstance().loadBan(readS(), readQ());
		}
	}

	@Override
	protected void runImpl() {
		log.info("Loaded " + count + " HDD ban entries.");
	}

}
