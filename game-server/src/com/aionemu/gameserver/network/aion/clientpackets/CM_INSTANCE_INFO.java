package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_INFO;
import com.aionemu.gameserver.utils.collections.FixedElementCountSplitList;
import com.aionemu.gameserver.utils.collections.SplitList;
import com.aionemu.gameserver.utils.collections.Predicates;

/**
 * @author nrg, Neon
 */
public class CM_INSTANCE_INFO extends AionClientPacket {

	private byte updateType; // 0 = reset to client default values and overwrite, 1 = update team member info, 2 = overwrite only

	public CM_INSTANCE_INFO(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		readD(); // unk (always 0)
		updateType = readC();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		Player firstObject = player.isInTeam() ? player.getCurrentTeam().getLeaderObject() : player; // always the team leader
		sendPacket(new SM_INSTANCE_INFO(updateType, firstObject));
		if (updateType == 1 && player.isInTeam()) {
			List<Player> filteredTeamMembers = player.getCurrentTeam().filterMembers(Predicates.Players.allExcept(firstObject));
			SplitList<Player> playersSplitList = new FixedElementCountSplitList<>(filteredTeamMembers, false, 3);
			playersSplitList.forEach(part -> sendPacket(new SM_INSTANCE_INFO((byte) 2, part)));
		}
	}
}
