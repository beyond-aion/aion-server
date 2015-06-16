package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerScripts;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_SCRIPTS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 */
public class CM_HOUSE_SCRIPT extends AionClientPacket {

	int address;
	int scriptIndex;
	int totalSize;
	int compressedSize;
	int uncompressedSize;
	byte[] stream;

	public CM_HOUSE_SCRIPT(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		address = readD();
		scriptIndex = readC();
		totalSize = readH();
		if (totalSize > 0) {
			compressedSize = readD();
			if (compressedSize < 8150) {
				uncompressedSize = readD();
				stream = readB(compressedSize);
			}
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (compressedSize > 8149) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_SCRIPT_OVERFLOW);
		}
		
		if(player.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MANAGE_HOUSE) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(player, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}

		House house = player.getActiveHouse();
		if (house == null)
			return;

		PlayerScripts scripts = house.getPlayerScripts();

		if (totalSize <= 0) {
			// Deposit perhaps should send 0, while delete -1
			// But the client sends the same packets now
			scripts.addScript(scriptIndex, new byte[0], 0);
		} else {
			scripts.addScript(scriptIndex, stream, uncompressedSize);
		}
		
		PacketSendUtility.sendPacket(player, new SM_HOUSE_SCRIPTS(address, scripts, scriptIndex, scriptIndex));
	}

}
