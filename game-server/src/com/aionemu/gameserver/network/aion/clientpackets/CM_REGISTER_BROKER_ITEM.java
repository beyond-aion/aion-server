package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.BrokerService;
import com.aionemu.gameserver.utils.PacketSendUtility;
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
		if (broker == null || broker.getObjectTemplate().getTalkInfo() == null || broker.getObjectTemplate().getTalkInfo().getFuncDialogIds() == null
			|| !broker.getObjectTemplate().getTalkInfo().getFuncDialogIds().contains(DialogAction.OPEN_VENDOR.id())) {
			AuditLogger.info(player, "Possibly modified packet to register broker item with no broker around.");
			return;
		}

		if (player.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_BROKER) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(player, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}

		BrokerService.getInstance().registerItem(player, itemUniqueId, itemCount, price, splittingAvailable);
	}
}
