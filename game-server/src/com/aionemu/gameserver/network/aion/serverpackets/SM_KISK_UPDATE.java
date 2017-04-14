package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sarynth
 */
public class SM_KISK_UPDATE extends AionServerPacket {

	private final Kisk kisk;

	public SM_KISK_UPDATE(Kisk kisk) {
		this.kisk = kisk;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(kisk.getObjectId());
		writeD(kisk.getCreatorId());
		writeD(kisk.getUseMask());
		writeD(kisk.getCurrentMemberCount());
		writeD(kisk.getMaxMembers());
		writeD(kisk.getRemainingResurrects());
		writeD(kisk.getMaxRessurects());
		writeD(kisk.getRemainingLifetime());
	}

}
