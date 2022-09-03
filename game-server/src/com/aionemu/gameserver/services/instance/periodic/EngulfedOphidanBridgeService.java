package com.aionemu.gameserver.services.instance.periodic;

import static com.aionemu.gameserver.configs.main.AutoGroupConfig.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Tibald, Sykra
 */
public class EngulfedOphidanBridgeService extends PeriodicInstance {

	private static final EngulfedOphidanBridgeService INSTANCE = new EngulfedOphidanBridgeService();

	public static EngulfedOphidanBridgeService getInstance() {
		return INSTANCE;
	}

	private EngulfedOphidanBridgeService() {
		super(ENGULFED_OPHIDAN_BRIDGE_TIMES, ENGULFED_OPHIDAN_BRIDGE_REGISTRATION_PERIOD, new int[] { 108 }, (byte) 60, (byte) 66);
	}

	@Override
	protected void sendEntry(Player player, int maskId) {
		super.sendEntry(player, maskId);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDLDF5_Under_01_War());
	}
}
