package com.aionemu.gameserver.network.aion.clientpackets;

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
 * @modified Neon
 */
public class CM_HOUSE_SCRIPT extends AionClientPacket {

	private final static int MAX_COMPRESSED_SIZE = 8149;
	int address;
	int scriptId;
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
		scriptId = readC();
		totalSize = readH();
		if (totalSize > 0) {
			compressedSize = readD();
			if (compressedSize <= MAX_COMPRESSED_SIZE) {
				uncompressedSize = readD();
				stream = readB(compressedSize);
			}
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (compressedSize > MAX_COMPRESSED_SIZE) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_SCRIPT_OVERFLOW());
			return;
		}

		House house = player.getActiveHouse();
		if (house == null)
			return;

		PlayerScripts scripts = house.getPlayerScripts();
		if (totalSize == 0) {
			scripts.remove(scriptId);
		} else {
			scripts.set(scriptId, stream, uncompressedSize);
		}

		PacketSendUtility.broadcastPacket(player, new SM_HOUSE_SCRIPTS(address, scripts, scriptId));
	}
}
