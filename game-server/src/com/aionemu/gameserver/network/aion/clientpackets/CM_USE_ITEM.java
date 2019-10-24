package com.aionemu.gameserver.network.aion.clientpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.actions.AbstractItemAction;
import com.aionemu.gameserver.model.templates.item.actions.IHouseObjectDyeAction;
import com.aionemu.gameserver.model.templates.item.actions.ItemActions;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Avol
 */
public class CM_USE_ITEM extends AionClientPacket {

	private int uniqueItemId;
	private byte type;
	private int targetItemId, indexReturn;

	public CM_USE_ITEM(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		uniqueItemId = readD();
		type = readC();
		switch (type) {
			case 2:
				targetItemId = readD();
				break;
			case 6:
				indexReturn = readD();
				break;
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (player.isProtectionActive()) {
			player.getController().stopProtectionActiveTask();
		}

		Item item = player.getInventory().getItemByObjId(uniqueItemId);
		Item targetItem = player.getInventory().getItemByObjId(targetItemId);
		HouseObject<?> targetHouseObject = null;

		if (item == null) {
			return;
		}

		if (targetItem == null)
			targetItem = player.getEquipment().getEquippedItemByObjId(targetItemId);
		if (targetItem == null && player.getActiveHouse() != null)
			targetHouseObject = player.getActiveHouse().getRegistry().getObjectByObjId(targetItemId);

		// check use item multicast delay exploit cast (spam)
		if (player.isCasting())
			player.getController().cancelCurrentSkill(null);

		if (!RestrictionsManager.canUseItem(player, item))
			return;

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

		if (item.getItemTemplate().getActivationRace() != null) {
			// TODO: check retail messages
			if (!(player.getTarget() instanceof Npc)) {
				PacketSendUtility.sendPacket(player, STR_ITEM_IS_NOT_USABLE());
				return;
			}
			Npc targetNpc = (Npc) player.getTarget();
			if (targetNpc.getRace() != item.getItemTemplate().getActivationRace()) {
				PacketSendUtility.sendPacket(player, STR_SKILL_CANT_CAST_TO_CURRENT_TARGET());
				return;
			}
		}
		HandlerResult result = QuestEngine.getInstance().onItemUseEvent(new QuestEnv(null, player, 0), item);
		if (result == HandlerResult.FAILED)
			return; // don't remove item

		ItemActions itemActions = item.getItemTemplate().getActions();
		if (itemActions == null || itemActions.getItemActions().isEmpty()) {
			PacketSendUtility.sendPacket(player, STR_ITEM_IS_NOT_USABLE());
			return;
		}
		// Prevents potion spamming, and relogging to use kisks/aether jelly/long CD items.
		if (player.hasCooldown(item)) {
			PacketSendUtility.sendPacket(player, STR_ITEM_CANT_USE_UNTIL_DELAY_TIME());
			return;
		}

		// for multi-return scrolls
		item.setIndexReturn(indexReturn);

		List<AbstractItemAction> actions = new ArrayList<>();
		for (AbstractItemAction itemAction : itemActions.getItemActions()) {
			// check if the item can be used before placing it on the cooldown list.
			if (targetHouseObject != null && itemAction instanceof IHouseObjectDyeAction) {
				IHouseObjectDyeAction action = (IHouseObjectDyeAction) itemAction;
				if (action.canAct(player, item, targetHouseObject))
					actions.add(itemAction);
			} else if (itemAction.canAct(player, item, targetItem))
				actions.add(itemAction);
		}

		if (actions.isEmpty())
			return; // notification should be handled in canAct

		// Store Item CD in server Player variable.
		int useDelay = player.getItemCooldown(item.getItemTemplate());
		if (useDelay > 0) {
			player.addItemCoolDown(item.getItemTemplate().getUseLimits().getDelayId(), System.currentTimeMillis() + useDelay, useDelay / 1000);
		}

		// notify item use observer
		player.getObserveController().notifyItemuseObservers(item);

		for (AbstractItemAction itemAction : actions) {
			if (targetHouseObject != null && itemAction instanceof IHouseObjectDyeAction) {
				IHouseObjectDyeAction action = (IHouseObjectDyeAction) itemAction;
				action.act(player, item, targetHouseObject);
			} else {
				itemAction.act(player, item, targetItem);
			}
		}
	}
}
