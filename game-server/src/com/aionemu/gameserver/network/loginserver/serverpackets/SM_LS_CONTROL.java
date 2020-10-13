package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;

/**
 * @author Aionchs-Wylovech
 */
public class SM_LS_CONTROL extends LsServerPacket {

	private final int type, param, accountId, adminId;

	public SM_LS_CONTROL(int type, int param, Player player, Player admin) {
		super(0x05);
		this.type = type;
		this.param = param;
		this.accountId = player.getAccount().getId();
		this.adminId = admin.getObjectId();
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeC(type);
		writeC(param);
		writeD(accountId);
		writeD(adminId);
	}
}
