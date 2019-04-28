package com.aionemu.gameserver.network.aion.clientpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.actions.AbstractItemAction;
import com.aionemu.gameserver.model.templates.item.actions.ItemActions;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MEGAPHONE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Artur, ginho1
 */
public class CM_MEGAPHONE extends AionClientPacket {

	private String message;
	private int uniqueItemId;

	public CM_MEGAPHONE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		message = readS();
		uniqueItemId = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		Item item = player.getInventory().getItemByObjId(uniqueItemId);
		if (item == null) {
			return;
		}

		if (item.getItemTemplate().getRace() != Race.PC_ALL && item.getItemTemplate().getRace() != player.getRace()) {
			PacketSendUtility.sendPacket(player, STR_CANNOT_USE_ITEM_INVALID_RACE());
			return;
		}

		if (!item.getItemTemplate().isClassSpecific(player.getCommonData().getPlayerClass())) {
			PacketSendUtility.sendPacket(player, STR_CANNOT_USE_ITEM_INVALID_CLASS());
			return;
		}

		int requiredLevel = item.getItemTemplate().getRequiredLevel(player.getPlayerClass());
		if (requiredLevel > player.getLevel()) {
			PacketSendUtility.sendPacket(player, STR_CANNOT_USE_ITEM_TOO_LOW_LEVEL_MUST_BE_THIS_LEVEL(item.getL10n(), requiredLevel));
			return;
		}

		byte levelRestrict = item.getItemTemplate().getMaxLevelRestrict(player.getPlayerClass());
		if (levelRestrict != 0 && player.getLevel() > levelRestrict) {
			PacketSendUtility.sendPacket(player, STR_CANNOT_USE_ITEM_TOO_HIGH_LEVEL(levelRestrict, item.getL10n()));
			return;
		}

		ItemActions itemActions = item.getItemTemplate().getActions();

		if (itemActions == null) {
			PacketSendUtility.sendPacket(player, STR_ITEM_IS_NOT_USABLE());
			return;
		}

		List<AbstractItemAction> actions = new ArrayList<>();

		for (AbstractItemAction itemAction : itemActions.getItemActions()) {
			if (itemAction.canAct(player, item, null))
				actions.add(itemAction);
		}

		if (actions.size() == 0) {
			PacketSendUtility.sendPacket(player, STR_ITEM_IS_NOT_USABLE());
			return;
		}

		if (player.hasCooldown(item)) {
			PacketSendUtility.sendPacket(player, STR_ITEM_CANT_USE_UNTIL_DELAY_TIME());
			return;
		}

		int useDelay = player.getItemCooldown(item.getItemTemplate());
		if (useDelay > 0) {
			player.addItemCoolDown(item.getItemTemplate().getUseLimits().getDelayId(), System.currentTimeMillis() + useDelay, useDelay / 1000);
		}

		// notify item use observer
		player.getObserveController().notifyItemuseObservers(item);

		for (AbstractItemAction itemAction : actions) {
			itemAction.act(player, item, null);
		}

		PacketSendUtility.broadcastToWorld(new SM_MEGAPHONE(player.getName(), message, item.getItemId()));
	}
}
