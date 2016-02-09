package com.aionemu.loginserver.network.aion.serverpackets;

import java.util.Collection;
import java.util.Map;

import com.aionemu.loginserver.GameServerInfo;
import com.aionemu.loginserver.GameServerTable;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.network.aion.AionServerPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;

/**
 * @author -Nemesiss-
 * @modified cura, Neon
 */
public class SM_SERVER_LIST extends AionServerPacket {

	public SM_SERVER_LIST() {
		super(0x04);
	}

	@Override
	protected void writeImpl(LoginConnection con) {
		Collection<GameServerInfo> servers = GameServerTable.getGameServers();
		Map<Integer, Integer> charactersCountOnServer = null;

		int accountId = con.getAccount().getId();
		int maxId = 0;

		charactersCountOnServer = AccountController.getGSCharacterCountsFor(accountId);

		writeC(servers.size());// servers
		writeC(con.getAccount().getLastServer());// last server
		for (GameServerInfo gsi : servers) {
			if (gsi.getId() > maxId)
				maxId = gsi.getId();

			writeC(gsi.getId());// server id
			writeB(gsi.getIp()); // server IP
			writeD(gsi.getPort());// port
			writeC(0x00); // age limit
			writeC(0x01);// pvp=1
			writeH(gsi.getCurrentPlayers());// currentPlayers
			writeH(gsi.getMaxPlayers());// maxPlayers
			writeC(gsi.isOnline() ? 1 : 0);// ServerStatus, up=1
			writeC(1);// bits);
			writeD(0);// server.brackets ? 0x01 : 0x00);
		}
		writeH(++maxId);
		writeC(0x01);

		for (int i = 1; i < maxId; i++) {
			if (charactersCountOnServer.containsKey(i))
				writeC(charactersCountOnServer.get(i));
			else
				writeC(0);
		}
		writeB(new byte[14]);
	}

}
