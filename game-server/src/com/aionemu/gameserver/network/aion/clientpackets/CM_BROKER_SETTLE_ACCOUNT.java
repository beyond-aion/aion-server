package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author kosyachok
 */
public class CM_BROKER_SETTLE_ACCOUNT extends AionClientPacket {

	private int brokerObjId;

	public CM_BROKER_SETTLE_ACCOUNT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		brokerObjId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (!player.isTargetingNpcWithFunctionNearby(brokerObjId, DialogAction.OPEN_VENDOR)) {
			AuditLogger.log(player, "tried to get Kinah and unsold items from the broker without targeting a broker in range");
			return;
		}
		BrokerService.getInstance().settleAccount(player);
	}
}
