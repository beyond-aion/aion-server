package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Tells the game client about character or legion name changes. It will then rename in all places like friend list, legion, group, housing npcs, etc.
 * 
 * @author Rhys2002
 */
public class SM_RENAME extends AionServerPacket {

	private final boolean isLegion;
	private final int playerOrLegionId;
	private final String oldName;
	private final String newName;

	public SM_RENAME(Player player, String oldName) {
		this(false, player.getObjectId(), oldName, player.getName());
	}

	public SM_RENAME(Legion legion, String oldName) {
		this(true, legion.getObjectId(), oldName, legion.getName());
	}

	private SM_RENAME(boolean isLegion, int playerOrLegionId, String oldName, String newName) {
		this.isLegion = isLegion;
		this.playerOrLegionId = playerOrLegionId;
		this.oldName = oldName;
		this.newName = newName;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(isLegion ? 1 : 0);
		writeD(0); // error code 3: name in use, 4: invalid name, 6: legion name in use, 7: invalid legion name, 8: legion holds keep, 9: legion disbanding
		writeD(playerOrLegionId);
		writeS(oldName);
		writeS(newName);
	}
}
