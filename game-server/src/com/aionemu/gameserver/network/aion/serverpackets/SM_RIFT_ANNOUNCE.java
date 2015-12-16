package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Map;

import com.aionemu.gameserver.controllers.RVController;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sweetkr
 * @modified -Enomine-
 */
public class SM_RIFT_ANNOUNCE extends AionServerPacket {

	private int actionId;
	private RVController rift;
	private Map<Integer, Integer> rifts;
	private int objectId;
	private int gelkmaros, inggison;
	private int cl, cr, tl, tr;

	/**
	 * Rift announce packet
	 *
	 * @param player
	 */
	public SM_RIFT_ANNOUNCE(Map<Integer, Integer> rifts) {
		this.actionId = 0;
		this.rifts = rifts;
	}

	public SM_RIFT_ANNOUNCE(boolean gelkmaros, boolean inggison) {
		this.gelkmaros = gelkmaros ? 1 : 0;
		this.inggison = inggison ? 1 : 0;
		this.actionId = 1;
	}

	/**
	 * Rift announce packet
	 *
	 * @param player
	 */
	public SM_RIFT_ANNOUNCE(RVController rift, boolean isMaster) {
		this.rift = rift;
		this.actionId = isMaster ? 2 : 3;
	}

	/**
	 * Rift despawn
	 *
	 * @param objectId
	 */
	public SM_RIFT_ANNOUNCE(int objectId) {
		this.objectId = objectId;
		this.actionId = 4;
	}

	public SM_RIFT_ANNOUNCE(boolean cl, boolean cr, boolean tl, boolean tr) {
		this.cl = cl ? 1 : 0; // Western Tiamaranta's Eye Entrance (Center left)
		this.cr = cr ? 1 : 0; // Eastern Tiamaranta's Eye Entrance (Center right)
		this.tl = tl ? 1 : 0; // Eye Abyss Gate Elyos (Top left)
		this.tr = tr ? 1 : 0; // Eye Abyss Gate Asmodians (Top right)
		this.actionId = 5;
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
				writeC(rift.isVortex() ? 1 : 0); // red | blue
				writeC(rift.isMaster() ? 1 : 0); // display | hide
				break;
			case 3:
				writeH(15); // following byte length
				writeC(actionId);
				writeD(rift.getOwner().getObjectId());
				writeD(rift.getUsedEntries());
				writeD(rift.getRemainTime());
				writeC(rift.isVortex() ? 1 : 0);
				writeC(0); // unk
				break;
			case 4: // rift despawn
				writeH(5); // following byte length
				writeC(actionId);
				writeD(objectId);
				break;
			case 5: // tiamaranta
				writeH(5); // following byte length
				writeC(actionId);
				writeC(cl);
				writeC(cr);
				writeC(tl);
				writeC(tr);
				break;
		}
	}
}
