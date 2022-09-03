package com.aionemu.gameserver.services.instance.periodic;

import static com.aionemu.gameserver.configs.main.AutoGroupConfig.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz, Sykra
 */
public class DredgionService extends PeriodicInstance {

	private static final DredgionService INSTANCE = new DredgionService();

	private final byte maskLvlGradeC = 1, maskLvlGradeB = 2, maskLvlGradeA = 3;

	public DredgionService() {
		super(DREDGION_TIMES, DREDGION_REGISTRATION_PERIOD, new int[] { 1, 2, 3 }, (byte) 45, (byte) 66);
	}

	public static DredgionService getInstance() {
		return INSTANCE;
	}

	@Override
	protected void sendEntry(Player player, int maskId) {
		boolean closeWindow = getInstanceMaskId(player) != maskId;
		PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, closeWindow));
		if (closeWindow)
			return;
		switch (maskId) {
			case maskLvlGradeC:
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDAB1_DREADGION());
				break;
			case maskLvlGradeB:
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDDREADGION_02());
				break;
			case maskLvlGradeA:
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDDREADGION_03());
				break;
		}
	}

	public byte getInstanceMaskId(Player player) {
		int level = player.getLevel();
		if (level < minLevel || level >= maxLevel)
			return 0;
		if (level < 51)
			return maskLvlGradeC;
		else if (level < 56)
			return maskLvlGradeB;
		return maskLvlGradeA;
	}

	@Override
	public void showWindow(Player player) {
		int maskId = getInstanceMaskId(player);
		if (maskId == 0)
			return;
		if (!hasCooldown(player))
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId));
	}

}
