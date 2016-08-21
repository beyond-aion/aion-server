package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.group.PlayerFilters.ExcludePlayerFilter;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_INFO;
import com.aionemu.gameserver.utils.collections.ListSplitter;

/**
 * @author nrg
 * @reworked Neon
 */
public class CM_INSTANCE_INFO extends AionClientPacket {

	private byte updateType; // 0 = reset to client default values and overwrite, 1 = update team member info, 2 = overwrite only

	public CM_INSTANCE_INFO(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		readD(); // unk (always 0)
		updateType = (byte) readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Player firstObject = player.isInTeam() ? player.getCurrentTeam().getLeaderObject() : player; // always the team leader
		sendPacket(new SM_INSTANCE_INFO(updateType, firstObject));
		if (updateType == 1 && player.isInTeam()) {
			ListSplitter<Player> splitter = new ListSplitter<>(player.getCurrentTeam().filterMembers(new ExcludePlayerFilter(firstObject)), 3, false);
			while (splitter.hasMore())
				sendPacket(new SM_INSTANCE_INFO((byte) 2, splitter.getNext())); // send info for max 3 members at once
		}
	}
}
