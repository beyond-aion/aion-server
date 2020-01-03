package com.aionemu.gameserver.services.instance.periodic;

import static com.aionemu.gameserver.configs.main.AutoGroupConfig.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Tibald, Sykra
 */
public class EngulfedOphidianBridgeService extends PeriodicInstance {

	private static final EngulfedOphidianBridgeService INSTANCE = new EngulfedOphidianBridgeService();

	public static EngulfedOphidianBridgeService getInstance() {
		return INSTANCE;
	}

	private EngulfedOphidianBridgeService() {
		super(ENGULFED_OB_ENABLE, ENGULFED_OB_TIMES, ENGULFED_OB_TIMER, new int[] { 108 }, (byte) 60, (byte) 66);
	}

	@Override
	protected void sendEntry(Player player, int maskId) {
		super.sendEntry(player, maskId);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDLDF5_Under_01_War());
	}
}
