package com.aionemu.gameserver.services.instance.periodic;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Tibald
 */
public class EngulfedOphidianBridgeService extends PeriodicInstance {

	private EngulfedOphidianBridgeService() {
		super(AutoGroupConfig.ENGULFED_OB_ENABLE, AutoGroupConfig.ENGULFED_OB_TIMES, AutoGroupConfig.ENGULFED_OB_TIMER, new byte[] { 108 }, (byte) 60,
			(byte) 66);
	}

	@Override
	protected void onSendEntry(Player player, byte maskId) {
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDLDF5_Under_01_War());
	}

	public static EngulfedOphidianBridgeService getInstance() {
		return EngulfedOphidianBridgeServiceHolder.INSTANCE;
	}

	private static class EngulfedOphidianBridgeServiceHolder {

		private static final EngulfedOphidianBridgeService INSTANCE = new EngulfedOphidianBridgeService();
	}
}
