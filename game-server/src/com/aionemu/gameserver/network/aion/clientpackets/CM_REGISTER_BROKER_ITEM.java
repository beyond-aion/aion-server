package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
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

	public CM_REGISTER_BROKER_ITEM(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
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

		VisibleObject broker = player.getTarget();
		if (!(broker instanceof Npc) || broker.getObjectId() != brokerObjId
			|| !((Npc) broker).getObjectTemplate().supportsAction(DialogAction.OPEN_VENDOR)) {
			AuditLogger.log(player, "possibly modified packet to register broker item with no broker in target");
			return;
		}

		BrokerService.getInstance().registerItem(player, itemUniqueId, itemCount, price, splittingAvailable);
	}
}
