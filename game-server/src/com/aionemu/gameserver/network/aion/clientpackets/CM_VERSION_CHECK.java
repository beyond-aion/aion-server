package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_VERSION_CHECK;
import com.aionemu.gameserver.services.event.EventService;

/**
 * @author -Nemesiss-
 */
public class CM_VERSION_CHECK extends AionClientPacket {

	private int aionClientVersion;
	@SuppressWarnings("unused")
	private int npcScriptInterfaceVersion;
	@SuppressWarnings("unused")
	private int windowsEncoding;
	@SuppressWarnings("unused")
	private int windowsVersion;
	@SuppressWarnings("unused")
	private int windowsSubVersion;
	@SuppressWarnings("unused")
	private int liteInfo;

	public CM_VERSION_CHECK(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		aionClientVersion = readUH();
		npcScriptInterfaceVersion = readUH();
		windowsEncoding = readD();
		windowsVersion = readD();
		windowsSubVersion = readD();
		liteInfo = readC(); // info if client is fully downloaded? seen values: 1, 2 (client checks for "2-ESSENTIAL" in data\lite\LiteGroupOrder.xml)
	}

	@Override
	protected void runImpl() {
		sendPacket(new SM_VERSION_CHECK(aionClientVersion, EventService.getInstance().getEventTheme()));
	}
}
