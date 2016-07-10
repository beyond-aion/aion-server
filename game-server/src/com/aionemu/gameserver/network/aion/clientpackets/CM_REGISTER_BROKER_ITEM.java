package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.World;

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

		if (player.getTarget() == null || player.getTarget().getObjectId() != brokerObjId) {
			AuditLogger.info(player, "Possibly modified packet to register broker item with no broker in target.");
			return;
		}

		Npc broker = World.getInstance().findNpc(brokerObjId);
		if (broker == null || !broker.getObjectTemplate().supportsAction(DialogAction.OPEN_VENDOR)) {
			AuditLogger.info(player, "Possibly modified packet to register broker item with no broker around.");
			return;
		}

		BrokerService.getInstance().registerItem(player, itemUniqueId, itemCount, price, splittingAvailable);
	}
}
