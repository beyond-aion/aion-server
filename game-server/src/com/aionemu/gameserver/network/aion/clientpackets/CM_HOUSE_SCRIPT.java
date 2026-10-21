package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerScripts;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_SCRIPTS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Rolandas, Neon, Sykra
 */
public class CM_HOUSE_SCRIPT extends AionClientPacket {

	/**
	 * Limit of the script block (both sizes and the compressed script), the client refuses to send larger ones.
	 */
	private static final int MAX_SCRIPT_BLOCK_SIZE = 3072;
	private int address;
	private int scriptId;
	private int totalSize;
	private int compressedSize;
	private int uncompressedSize;
	private byte[] scriptContent;

	public CM_HOUSE_SCRIPT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		address = readD();
		scriptId = readUC();
		totalSize = readUH();
		if (totalSize > 0 && totalSize <= MAX_SCRIPT_BLOCK_SIZE) {
			compressedSize = readD();
			uncompressedSize = readD();
			if (compressedSize >= 0 && compressedSize <= totalSize - 8)
				scriptContent = readB(compressedSize);
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (totalSize > MAX_SCRIPT_BLOCK_SIZE) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_SCRIPT_OVERFLOW());
			return;
		}
		if (totalSize > 0 && scriptContent == null) {
			AuditLogger.log(player, "sent a house script of " + compressedSize + " bytes in a block of " + totalSize + " bytes");
			return;
		}
		House house = player.getActiveHouse();
		if (house == null || house.getAddress().getId() != address) {
			AuditLogger.log(player, "tried to modify script of house they don't own (address " + address + ")");
			return;
		}
		if (!house.isOnLand(player))
			return;
		PlayerScripts scripts = house.getPlayerScripts();
		if (totalSize == 0)
			scripts.remove(scriptId);
		else
			scripts.set(scriptId, scriptContent, uncompressedSize);
		PacketSendUtility.broadcastPacket(player, new SM_HOUSE_SCRIPTS(address, scripts.get(scriptId)));
	}
}
