package com.aionemu.gameserver.network.aion.serverpackets;

import javolution.util.FastMap;

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
	private FastMap<Integer, Integer> rifts;
	private int objectId;
	private int gelkmaros, inggison;
	private int cl, cr, tl, tr;

	/**
	 * Rift announce packet
	 *
	 * @param player
	 */
	public SM_RIFT_ANNOUNCE(FastMap<Integer, Integer> rifts) {
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
		this.tr = tr ? 1 : 0; // Eye Abyss Gate Asmodians (Top rigft)
		this.actionId = 5;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		switch (actionId) {
			case 0: // announce
				writeH(0x19);// 4.0 // old -->writeH(0x11); // 0x11
				writeC(actionId);
				for (int value : rifts.values()) {
					writeD(value);
				}
				break;
			case 1:
				writeH(0x09); // 0x09
				writeC(actionId);
				writeD(gelkmaros);
				writeD(inggison);
				break;
			case 2:
				writeH(0x23); // 0x23
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
				writeH(0x0f); // 0x0f
				writeC(actionId);
				writeD(rift.getOwner().getObjectId());
				writeD(rift.getUsedEntries());
				writeD(rift.getRemainTime());
				writeC(rift.isVortex() ? 1 : 0);
				writeC(0); // unk
				break;
			case 4:
				writeH(0x05);
				writeC(actionId);
				writeD(objectId);
				break;
			case 5:
				writeH(0x05); // 0x05
				writeC(actionId);
				writeC(cl);
				writeC(cr);
				writeC(tl);
				writeC(tr);
				break;

		}
	}

}
