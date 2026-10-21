package com.aionemu.loginserver.network.aion.serverpackets;

import java.util.Collection;
import java.util.Map;

import com.aionemu.loginserver.GameServerInfo;
import com.aionemu.loginserver.GameServerTable;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.network.aion.AionServerPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;

/**
 * @author -Nemesiss-, cura, Neon
 */
public class SM_SERVER_LIST extends AionServerPacket {

	public SM_SERVER_LIST() {
		super(0x04);
	}

	@Override
	protected void writeImpl(LoginConnection con) {
		Collection<GameServerInfo> servers = GameServerTable.getGameServers();
		Map<Byte, Integer> charCountOnServer = AccountController.getGSCharacterCountsFor(con.getAccount().getId());
		int maxIdWithChars = 0;

		writeC(servers.size()); // loop size
		writeC(con.getAccount().getLastServer());
		for (GameServerInfo gsi : servers) {
			byte gsId = gsi.getId();
			if (gsId > maxIdWithChars && charCountOnServer.get(gsId) != null)
				maxIdWithChars = gsi.getId();
			writeC(gsId);
			writeB(gsi.getIp());
			writeH(gsi.getPort());
			writeH(0); // the client reads it with the port as one int and uses only the port
			writeC(0); // age limit in the Lineage 2 protocol this one derives from; the 4.8 client copies it and never reads it
			writeC(0); // PvP flag in the Lineage 2 protocol; the 4.8 client copies it and never reads it
			writeH(gsi.getCurrentPlayers());
			writeH(gsi.getMaxPlayers());
			writeC(gsi.isOnline() ? 1 : 0);
			// type mask: the client lists the server if bit 0 is set (bit 2 for a client in test server mode);
			// bit 8 keeps it out of the "Recommended server is ..." prompt that a player without characters gets
			writeD(1);
			writeC(0); // country code: with country code 5 in the client's cc.ini, servers of other countries are hidden; 0 shows it to all
		}
		writeH(maxIdWithChars + 1); // size of the following block
		writeC(1); // 1: the number of characters per server follows
		for (byte gsId = 1; gsId <= maxIdWithChars; gsId++)
			writeC(charCountOnServer.getOrDefault(gsId, 0));
	}
}
