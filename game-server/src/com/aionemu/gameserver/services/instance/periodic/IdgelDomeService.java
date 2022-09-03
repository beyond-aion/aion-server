package com.aionemu.gameserver.services.instance.periodic;

import static com.aionemu.gameserver.configs.main.AutoGroupConfig.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Ritsu, Sykra
 */
public class IdgelDomeService extends PeriodicInstance {

	private static final IdgelDomeService INSTANCE = new IdgelDomeService();

	public static IdgelDomeService getInstance() {
		return INSTANCE;
	}

	private IdgelDomeService() {
		super(IDGEL_DOME_TIMES, IDGEL_DOME_REGISTRATION_PERIOD, new int[] { 111 }, (byte) 60, (byte) 66);
	}

	@Override
	protected void sendEntry(Player player, int maskId) {
		super.sendEntry(player, maskId);
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDLDF5_Fortress_Re());
	}

}
