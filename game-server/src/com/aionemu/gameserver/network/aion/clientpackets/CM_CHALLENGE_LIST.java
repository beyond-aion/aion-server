package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.challenge.ChallengeType;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.ChallengeTaskService;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Rolandas
 */
public class CM_CHALLENGE_LIST extends AionClientPacket {

	public CM_CHALLENGE_LIST(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	int action;
	int taskOwner;
	int ownerType;
	int playerId;
	int dateSince;

	@Override
	protected void readImpl() {
		action = readUC();
		taskOwner = readD();
		ownerType = readUC();
		playerId = readD();
		dateSince = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (ownerType == 1) {
			if (player.getLegion() == null) {
				AuditLogger.log(player, "tried to receive legion challenge task without legion");
				return;
			}
			ChallengeTaskService.getInstance().showTaskList(player, ChallengeType.LEGION, taskOwner);
		} else {
			ChallengeTaskService.getInstance().showTaskList(player, ChallengeType.TOWN, taskOwner);
		}
	}

}
