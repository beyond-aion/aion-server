package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerScripts;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_SCRIPTS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas, Neon, Sykra
 */
public class CM_HOUSE_SCRIPT extends AionClientPacket {

	/**
	 * Maximum (compressed) size of a script. The size is determined by subtracting the maximum usable packet body size in bytes by the overhead bytes
	 * required to send a single script via SM_HOUSE_SCRIPTS
	 */
	private final static int MAX_COMPRESSED_SIZE = AionServerPacket.MAX_USABLE_PACKET_BODY_SIZE - 136;

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
		if (totalSize > 0) {
			compressedSize = readD();
			if (compressedSize <= MAX_COMPRESSED_SIZE) {
				uncompressedSize = readD();
				scriptContent = readB(compressedSize);
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
		if (totalSize == 0)
			scripts.remove(scriptId);
		else
			scripts.set(scriptId, scriptContent, uncompressedSize);
		PacketSendUtility.broadcastPacket(player, new SM_HOUSE_SCRIPTS(address, scripts, scriptId));
	}
}
