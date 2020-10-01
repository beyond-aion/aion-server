package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.actions.AbstractItemAction;
import com.aionemu.gameserver.model.templates.item.actions.DyeAction;
import com.aionemu.gameserver.model.templates.item.actions.InstanceTimeClear;
import com.aionemu.gameserver.model.templates.item.actions.MultiReturnAction;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.restrictions.PlayerRestrictions;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Avol, Neon
 */
public class CM_USE_ITEM extends AionClientPacket {

	private int uniqueItemId;
	private int targetItemId, syncId, indexReturn;

	public CM_USE_ITEM(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		uniqueItemId = readD();
		byte type = readC();
		switch (type) {
			case 2:
				targetItemId = readD();
				break;
			case 5: // instance cooltime reset scroll
				syncId = readD();
				break;
			case 6:
				indexReturn = readD();
				break;
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (player.isProtectionActive())
			player.getController().stopProtectionActiveTask();

		Item item = player.getInventory().getItemByObjId(uniqueItemId);
		if (item == null)
			return;

		Item targetItem = null;
		HouseObject<?> targetHouseObject = null;
		if (targetItemId != 0) {
			targetItem = player.getInventory().getItemByObjId(targetItemId);
			if (targetItem == null)
				targetItem = player.getEquipment().getEquippedItemByObjId(targetItemId);
			if (targetItem == null && player.getActiveHouse() != null)
				targetHouseObject = player.getActiveHouse().getRegistry().getObjectByObjId(targetItemId);
		}

		// check use item multicast delay exploit cast (spam)
		if (player.isCasting())
			player.getController().cancelCurrentSkill(null);

		if (!PlayerRestrictions.canUseItem(player, item))
			return;

		HandlerResult result = QuestEngine.getInstance().onItemUseEvent(new QuestEnv(null, player, 0), item);

		List<AbstractItemAction> itemActions = item.getItemTemplate().getActions() == null ? Collections.emptyList()
			: item.getItemTemplate().getActions().getItemActions();

		if (itemActions.isEmpty() && result != HandlerResult.SUCCESS) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_IS_NOT_USABLE());
			return;
		}

		List<AbstractItemAction> actions = new ArrayList<>();
		for (AbstractItemAction itemAction : itemActions) {
			// check if the item can be used before placing it on the cooldown list.
			if (itemAction instanceof DyeAction) {
				if (itemAction.canAct(player, item, targetItem, targetHouseObject))
					actions.add(itemAction);
			} else if (itemAction instanceof MultiReturnAction) {
				if (itemAction.canAct(player, item, targetItem, indexReturn))
					actions.add(itemAction);
			} else if (itemAction instanceof InstanceTimeClear) {
				if (itemAction.canAct(player, item, targetItem, syncId))
					actions.add(itemAction);
			} else if (itemAction.canAct(player, item, targetItem)) {
				actions.add(itemAction);
			}
		}

		if (actions.isEmpty())
			return; // notification should be handled in canAct

		int useDelay = item.getItemTemplate().getUseLimits().getDelayTime();
		if (useDelay > 0)
			player.addItemCoolDown(item.getItemTemplate().getUseLimits().getDelayId(), System.currentTimeMillis() + useDelay, useDelay / 1000);

		// notify item use observer
		player.getObserveController().notifyItemuseObservers(item);

		for (AbstractItemAction itemAction : actions) {
			if (itemAction instanceof DyeAction) {
				itemAction.act(player, item, targetItem, targetHouseObject);
			} else if (itemAction instanceof MultiReturnAction) {
				itemAction.act(player, item, targetItem, indexReturn);
			} else if (itemAction instanceof InstanceTimeClear) {
				itemAction.act(player, item, targetItem, syncId);
			} else {
				itemAction.act(player, item, targetItem);
			}
		}
	}
}
