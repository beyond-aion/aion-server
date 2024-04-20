package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Map;

import com.aionemu.gameserver.controllers.RVController;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sweetkr, -Enomine-
 */
public class SM_RIFT_ANNOUNCE extends AionServerPacket {

	private int actionId;
	private RVController rift;
	private Map<Integer, Integer> rifts;
	private int objectId;
	private int gelkmaros, inggison;

	public SM_RIFT_ANNOUNCE(Map<Integer, Integer> rifts) {
		this.actionId = 0;
		this.rifts = rifts;
	}

	public SM_RIFT_ANNOUNCE(boolean gelkmaros, boolean inggison) {
		this.gelkmaros = gelkmaros ? 1 : 0;
		this.inggison = inggison ? 1 : 0;
		this.actionId = 1;
	}

	public SM_RIFT_ANNOUNCE(RVController rift, boolean isMaster) {
		this.rift = rift;
		this.actionId = isMaster ? 2 : 3;
	}

	public SM_RIFT_ANNOUNCE(int objectId) {
		this.objectId = objectId;
		this.actionId = 4;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		switch (actionId) {
			case 0: // announce
				writeH(1 + (rifts.values().size() * 4)); // following byte length
				writeC(actionId);
				for (int value : rifts.values())
					writeD(value);
				break;
			case 1: // silentera
				writeH(9); // following byte length
				writeC(actionId);
				writeD(gelkmaros);
				writeD(inggison);
				break;
			case 2:
				writeH(35); // following byte length
				writeC(actionId);
				writeD(rift.getOwner().getObjectId());
				writeD(rift.getMaxEntries());
				writeD(rift.getRemainTime());
				writeD(rift.getMinLevel());
				writeD(rift.getMaxLevel());
				writeF(rift.getOwner().getX());
				writeF(rift.getOwner().getY());
				writeF(rift.getOwner().getZ());
				writeRiftType();
				writeC(rift.isMaster() ? 1 : 0); // display | hide
				break;
			case 3:
				writeH(15); // following byte length
				writeC(actionId);
				writeD(rift.getOwner().getObjectId());
				writeD(rift.getUsedEntries());
				writeD(rift.getRemainTime());
				writeRiftType();
				writeC(0); // unk
				break;
			case 4: // rift despawn
				writeH(5); // following byte length
				writeC(actionId);
				writeD(objectId);
				break;
		}
	}

	private void writeRiftType() {
		// 1 vortex, 2, concert hall, 3 pangaea, 4 chaos rift, 5 infiltration rift
		if (rift.isVortex()) {
			writeC(1);
		} else if (rift.isVolatile()) {
			writeC(4);
		} else if (rift.isInvasion()) {
			writeC(5);
		} else {
			writeC(0);
		}
	}

}
