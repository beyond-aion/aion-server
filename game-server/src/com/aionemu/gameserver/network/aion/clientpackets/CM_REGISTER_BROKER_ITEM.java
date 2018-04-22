package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author kosyak
 */
public class CM_REGISTER_BROKER_ITEM extends AionClientPacket {

	private int brokerObjId;
	private int itemUniqueId;
	private long price;
	private long itemCount;
	private boolean splittingAvailable;

	public CM_REGISTER_BROKER_ITEM(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		brokerObjId = readD();
		itemUniqueId = readD();
		price = readQ();
		itemCount = readQ();
		splittingAvailable = readC() == 1;
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (player.isTrading() || itemCount <= 0)
			return;

		if (player.isTargetingNpcWithFunction(brokerObjId, DialogAction.OPEN_VENDOR))
			BrokerService.getInstance().registerItem(player, itemUniqueId, itemCount, price, splittingAvailable);
		else
			AuditLogger.log(player, "tried to register a broker item without targeting a broker");
	}
}
