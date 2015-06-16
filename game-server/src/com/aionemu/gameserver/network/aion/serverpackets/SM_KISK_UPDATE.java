package com.aionemu.gameserver.network.aion.serverpackets;


import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sarynth 0xB0 for 1.5.1.10 and 1.5.1.15
 */
public class SM_KISK_UPDATE extends AionServerPacket {

	// useMask values determine who can bind to the kisk.
	// 1 ~ race
	// 2 ~ legion
	// 3 ~ solo
	// 4 ~ group
	// 5 ~ alliance
	// of course, we must programmatically check as well.

	private int objId;
	private int creatorId;
	private int useMask;
	private int currentMembers;
	private int maxMembers;
	private int remainingRessurects;
	private int maxRessurects;
	private int remainingLifetime;

	public SM_KISK_UPDATE(Kisk kisk) {
		this.objId = kisk.getObjectId();
		this.creatorId = kisk.getCreatorId();
		this.useMask = kisk.getUseMask();
		this.currentMembers = kisk.getCurrentMemberCount();
		this.maxMembers = kisk.getMaxMembers();
		this.remainingRessurects = kisk.getRemainingResurrects();
		this.maxRessurects = kisk.getMaxRessurects();
		this.remainingLifetime = kisk.getRemainingLifetime();
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(objId);
		writeD(creatorId);
		writeD(useMask);
		writeD(currentMembers);
		writeD(maxMembers);
		writeD(remainingRessurects);
		writeD(maxRessurects);
		writeD(remainingLifetime);
	}

}
