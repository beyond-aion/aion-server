package com.aionemu.gameserver.services.instance.periodic;

import java.util.Iterator;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author xTz
 */
public class DredgionService2 extends PeriodicInstance {

	private final byte maskLvlGradeC = 1, maskLvlGradeB = 2, maskLvlGradeA = 3;

	public DredgionService2() {
		super(AutoGroupConfig.DREDGION2_ENABLE, AutoGroupConfig.DREDGION_TIMES, AutoGroupConfig.DREDGION_TIMER, new byte[] { 1, 2, 3 }, (byte) 45,
			(byte) 66);
	}

	@Override
	public void startRegistration() {
		this.registerAvailable = true;
		startUnregisterTask();
		Iterator<Player> iter = World.getInstance().getPlayersIterator();
		while (iter.hasNext()) {
			Player player = iter.next();
			if (player.getLevel() > minLevel && player.getLevel() <= maxLevel) {
				for (byte maskId : this.maskIds) {
					PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, getInstanceMaskId(player) != maskId));
					onSendEntry(player, maskId);
				}
			}
		}
	}

	@Override
	protected void onSendEntry(Player player, byte maskId) {
		switch (maskId) {
			case maskLvlGradeC:
				if (getInstanceMaskId(player) == maskId)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDAB1_DREADGION());
				break;
			case maskLvlGradeB:
				if (getInstanceMaskId(player) == maskId)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDDREADGION_02());
				break;
			case maskLvlGradeA:
				if (getInstanceMaskId(player) == maskId)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INSTANCE_OPEN_IDDREADGION_03());
				break;
		}
	}

	public byte getInstanceMaskId(Player player) {
		int level = player.getLevel();
		if (level < minLevel || level >= maxLevel) {
			return 0;
		}
		if (level < 51) {
			return this.maskLvlGradeC;
		} else if (level < 56) {
			return this.maskLvlGradeB;
		} else {
			return this.maskLvlGradeA;
		}
	}

	@Override
	public void showWindow(Player player) {
		int maskId = getInstanceMaskId(player);
		if (maskId == 0) {
			return;
		}
		if (!this.playersWithCooldown.contains(player.getObjectId())) {
			PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId));
		}
	}

	private static class SingletonHolder {

		protected static final DredgionService2 instance = new DredgionService2();
	}

	public static DredgionService2 getInstance() {
		return SingletonHolder.instance;
	}

}
