package com.aionemu.gameserver.services.instance.periodic;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Ritsu
 */
public class IdgelDomeService extends PeriodicInstance {

	private IdgelDomeService() {
		super(AutoGroupConfig.IDGEL_DOME_ENABLE, AutoGroupConfig.IDGEL_DOME_TIMES, AutoGroupConfig.IDGEL_DOME_TIMER, new byte[] { 111 }, (byte) 60,
			(byte) 66);
	}

	@Override
	protected void onSendEntry(Player player, byte maskId) {
		PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402192));
	}

	private static class SingletonHolder {

		protected static final IdgelDomeService instance = new IdgelDomeService();
	}

	public static IdgelDomeService getInstance() {
		return SingletonHolder.instance;
	}
}
