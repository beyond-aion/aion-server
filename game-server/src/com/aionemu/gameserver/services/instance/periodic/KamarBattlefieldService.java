package com.aionemu.gameserver.services.instance.periodic;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
public class KamarBattlefieldService extends PeriodicInstance {

	private KamarBattlefieldService() {
		super(AutoGroupConfig.KAMAR_ENABLE, AutoGroupConfig.KAMAR_TIMES, AutoGroupConfig.KAMAR_TIMER, new byte[] { 107 }, (byte) 60, (byte) 66);
	}

	@Override
	protected void onSendEntry(Player player, byte maskId) {
		PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401730));
	}

	private static class SingletonHolder {

		protected static final KamarBattlefieldService instance = new KamarBattlefieldService();
	}

	public static KamarBattlefieldService getInstance() {
		return SingletonHolder.instance;
	}
}
