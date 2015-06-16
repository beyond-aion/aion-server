package com.aionemu.gameserver.services.instance.periodic;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 *
 * @author Tibald
 */
public class IronWallFrontService extends PeriodicInstance {
	
	private IronWallFrontService() {
		super(AutoGroupConfig.IRON_WALL_FRONT_ENABLE, AutoGroupConfig.IRON_WALL_FRONT_TIMES, AutoGroupConfig.IRON_WALL_FRONT_TIMER, new byte[] {109}, (byte) 60, (byte) 66);
	}
   
	@Override
	protected void onSendEntry(Player player, byte maskId) {
        PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1402032));
    }
   
	private static class SingletonHolder {
		protected static final IronWallFrontService instance = new IronWallFrontService();
	}

	public static IronWallFrontService getInstance() {
		return SingletonHolder.instance;
	}
}
