package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.actions.MegaphoneAction;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.restrictions.PlayerRestrictions;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Receives a request to send a message to the global "Faction" chat.
 * Client must be started with -megaphone in order to send this packet. It is sent when using an item like 188910000.
 * 
 * @author Artur, ginho1, Neon
 */
public class CM_MEGAPHONE extends AionClientPacket {

	private String message;
	private int itemObjId;

	public CM_MEGAPHONE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		message = readS();
		itemObjId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		Item item = player.getInventory().getItemByObjId(itemObjId);
		if (item == null)
			return;

		if (!PlayerRestrictions.canUseItem(player, item))
			return;

		MegaphoneAction megaphoneAction = item.getItemTemplate().getActions().getItemActions().stream()
				.filter(a -> a instanceof MegaphoneAction)
				.map(a -> (MegaphoneAction) a)
				.findAny().orElse(null);
		if (megaphoneAction == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_IS_NOT_USABLE());
			return;
		}
		if (megaphoneAction.canAct(player, item, null, message)) {
			int useDelay = item.getItemTemplate().getUseLimits().getDelayTime();
			if (useDelay > 0)
				player.addItemCoolDown(item.getItemTemplate().getUseLimits().getDelayId(), System.currentTimeMillis() + useDelay, useDelay / 1000);
			player.getObserveController().notifyItemuseObservers(item);
			megaphoneAction.act(player, item, null, message);
		}
	}
}
