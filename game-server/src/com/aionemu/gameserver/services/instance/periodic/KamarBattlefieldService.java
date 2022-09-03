package com.aionemu.gameserver.services.instance.periodic;

import static com.aionemu.gameserver.configs.main.AutoGroupConfig.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz, Sykra
 */
public class KamarBattlefieldService extends PeriodicInstance {

	private static final KamarBattlefieldService INSTANCE = new KamarBattlefieldService();

	public static KamarBattlefieldService getInstance() {
		return INSTANCE;
	}

	private KamarBattlefieldService() {
		super(KAMAR_BATTLEFIELD_TIMES, KAMAR_BATTLEFIELD_REGISTRATION_PERIOD, new int[] { 107 }, (byte) 60, (byte) 66);
	}

	@Override
	protected void sendEntry(Player player, int maskId) {
		super.sendEntry(player, maskId);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDKamar());
	}

}
