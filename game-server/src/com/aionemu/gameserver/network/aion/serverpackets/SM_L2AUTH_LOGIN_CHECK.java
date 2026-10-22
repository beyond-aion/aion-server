package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author -Nemesiss-
 */
public class SM_L2AUTH_LOGIN_CHECK extends AionServerPacket {

	private static final byte[] serverIdByIndex = new byte[128];
	private static final byte[] serverIndexById = new byte[64];

	static {
		// retail data, don't question it
		for (byte i = 1; i <= 60; i++) {
			serverIdByIndex[i] = i;
			serverIndexById[i] = i;
		}
		serverIdByIndex[66] = 61;
		serverIndexById[61] = 66;
	}

	/**
	 * True if client is authed.
	 */
	private final boolean ok;
	private final String accountName;

	public SM_L2AUTH_LOGIN_CHECK(boolean ok, String accountName) {
		this.ok = ok;
		this.accountName = accountName;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(ok ? 0x00 : 0x01);
		writeC(0); // server ID override (added in 4.7)
		writeC(0); // 1 on Fast-Track Server: makes the client send C_REQUEST_DIRECT_ENTER_WORLD
		writeC(0); // 1 on Fast-Track Server: displays the origin server's name above the minimap and as system message
		writeC(0); // 1 on Fast-Track Server
		for (int i = 0; i < serverIdByIndex.length; i++) {
			byte serverId = serverIdByIndex[i];
			writeC(serverId == 0 ? 0 : i);
			writeC(serverId);
			writeC(serverId);
		}
		for (int serverId = 0; serverId < serverIndexById.length; serverId++) {
			byte i = serverIndexById[serverId];
			writeC(i);
			writeC(i == 0 ? 0 : serverId);
			writeC(i == 0 ? 0 : serverId);
		}
		writeH(DataManager.WORLD_MAPS_DATA.size());
		for (WorldMapTemplate template : DataManager.WORLD_MAPS_DATA) {
			writeD(template.getMapId());
			writeH(template.isInstance() ? 0 : template.getTwinCount()); // for Fast-Track Server it's getBeginnerTwinCount()
		}
		writeS(accountName);
	}
}
