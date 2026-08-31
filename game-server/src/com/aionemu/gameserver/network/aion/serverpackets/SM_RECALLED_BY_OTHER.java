package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Opens the window which asks a player whether he wants to be teleported to the caster of a summon skill, and closes it again when the request is
 * no longer valid. The client answers with CM_RECALLED_BY_OTHER_ANSWER.
 * 
 * @author SVDNESS
 */
public class SM_RECALLED_BY_OTHER extends AionServerPacket {
	
	private static final int OPEN_WINDOW = 0;
	private static final int CANCELLED = 1;
	private final int result;
	private final String casterName;
	private final int skillId;
	private final int seconds;
	
	public static SM_RECALLED_BY_OTHER cancelled() {
		return new SM_RECALLED_BY_OTHER(CANCELLED, null, 0, 0);
	}

	public SM_RECALLED_BY_OTHER(String casterName, int skillId, int seconds) {
		this(OPEN_WINDOW, casterName, skillId, seconds);
	}

	private SM_RECALLED_BY_OTHER(int result, String casterName, int skillId, int seconds) {
		this.result = result;
		this.casterName = casterName;
		this.skillId = skillId;
		this.seconds = seconds;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(result);
		writeS(casterName);
		writeH(skillId);
		writeH(seconds);
	}
}