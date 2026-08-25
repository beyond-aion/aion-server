package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Opens the window which asks a player whether he wants to be teleported to the caster of a summon skill, and closes it again when the request is
 * no longer valid. The client answers with CM_RECALLED_BY_OTHER_ANSWER.
 */
public class SM_RECALLED_BY_OTHER extends AionServerPacket {

	private final String casterName;
	private final int skillId;
	private final int seconds;

	/**
	 * Closes the window on the client.
	 */
	public SM_RECALLED_BY_OTHER() {
		this(null, 0, 0);
	}

	/**
	 * @param casterName
	 *          - name of the summoning player
	 * @param skillId
	 *          - skill he used, its name is displayed in the window
	 * @param seconds
	 *          - time the player has to answer
	 */
	public SM_RECALLED_BY_OTHER(String casterName, int skillId, int seconds) {
		this.casterName = casterName;
		this.skillId = skillId;
		this.seconds = seconds;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(casterName == null ? 1 : 0); // 0 = open the window, 1 = close it
		writeS(casterName);
		writeH(skillId);
		writeH(seconds);
	}
}
