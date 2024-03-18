package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sweetkr
 */
public class SM_CUSTOM_SETTINGS extends AionServerPacket {

	public static final int HIDE_LEGION_CLOAK = 1;
	public static final int HIDE_LEGION_CLOAK_BY_WEAPON_PRIORITY = 2;
	public static final int HIDE_HELMET = 4;
	public static final int HIDE_PLUME = 8;

	private int objectId;
	private int unk = 0;
	private int display; // bitmask of HIDE_* values
	private int deny;

	public SM_CUSTOM_SETTINGS(Player player) {
		this(player.getObjectId(), 1, player.getPlayerSettings().getDisplay(), player.getPlayerSettings().getDeny());
	}

	public SM_CUSTOM_SETTINGS(int objectId, int unk, int display, int deny) {
		this.objectId = objectId;
		this.display = display;
		this.deny = deny;
		this.unk = unk;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(objectId);
		writeC(unk); // unk
		writeH(display);
		writeH(deny);
	}
}
