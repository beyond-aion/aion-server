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
			writeH(0); // unk, always 0
			writeC(0); // age limit (?)
			writeC(0); // pvp=1 (?)
			writeH(gsi.getCurrentPlayers());
			writeH(gsi.getMaxPlayers());
			writeC(gsi.isOnline() ? 1 : 0);
			writeC(1); // server type (1 = normal, 4 = test server)
			writeC(0); // hide server from list (beginner or panesterra server) ? 1 : 0
			writeH(0); // unk, always 0
			writeC(0);// server.brackets ? 1 : 0
		}
		writeH(maxIdWithChars + 1);
		writeC(1); // enable "last server" button & auto-connection ? 1 : 0
		for (byte gsId = 1; gsId <= maxIdWithChars; gsId++)
			writeC(charCountOnServer.getOrDefault(gsId, 0));
		writeB(new byte[13]); // unk (44 77 90 A8 5D 75 C9 98 6D 20 53 2A 97)
	}
}
