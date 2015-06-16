package com.aionemu.gameserver.network.aion.clientpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_CLASS;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_RACE;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_TOO_HIGH_LEVEL;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_TOO_LOW_LEVEL_MUST_BE_THIS_LEVEL;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_ITEM_CANT_USE_UNTIL_DELAY_TIME;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_ERROR;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_ITEM_IS_NOT_USABLE;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_SKILL_CANT_CAST_TO_CURRENT_TARGET;

import java.util.ArrayList;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
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
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.restrictions.RestrictionsManager;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Avol
 */
public class CM_USE_ITEM extends AionClientPacket {

	public int uniqueItemId;
	public int type, targetItemId, indexReturn = 0;

	public CM_USE_ITEM(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if(player.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_USE_ITEMS) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(player, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}
		
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
		if (targetItem == null && player.getHouseRegistry() != null)
			targetHouseObject = player.getHouseRegistry().getObjectByObjId(targetItemId);

		if (item.getItemTemplate().getTemplateId() == 165000001 && targetItem.getItemTemplate().canExtract()) {
			PacketSendUtility.sendPacket(player, STR_ITEM_COLOR_ERROR);
			return;
		}
		

		// check use item multicast delay exploit cast (spam)
		if (player.isCasting()) {
			// PacketSendUtility.sendMessage(this.getOwner(),
			// "You must wait until cast time finished to use skill again.");
			player.getController().cancelCurrentSkill();
			// On retail the item is cancelling the current skill and then procs normally
			// return;
		}

		if (!RestrictionsManager.canUseItem(player, item))
			return;

		if (item.getItemTemplate().getRace() != Race.PC_ALL && item.getItemTemplate().getRace() != player.getRace()) {
			PacketSendUtility.sendPacket(player, STR_CANNOT_USE_ITEM_INVALID_RACE);
			return;
		}
		
		if (!item.getItemTemplate().isClassSpecific(player.getCommonData().getPlayerClass())) {
			PacketSendUtility.sendPacket(player, STR_CANNOT_USE_ITEM_INVALID_CLASS);
			return;
		}
		
		int requiredLevel = item.getItemTemplate().getRequiredLevel(player.getPlayerClass());
		if (requiredLevel > player.getLevel()) {
			PacketSendUtility.sendPacket(player,
				STR_CANNOT_USE_ITEM_TOO_LOW_LEVEL_MUST_BE_THIS_LEVEL(item.getNameId(), requiredLevel));
			return;
		}
		
		byte levelRestrict = item.getItemTemplate().getMaxLevelRestrict(player.getPlayerClass());
		if (levelRestrict != 0 && player.getLevel() > levelRestrict) {
			PacketSendUtility.sendPacket(player, STR_CANNOT_USE_ITEM_TOO_HIGH_LEVEL(levelRestrict, item.getNameId()));
			return;
		}

		if (item.getItemTemplate().getActivationRace() != null) {
			// TODO: check retail messages
			if (!(player.getTarget() instanceof Npc)) {
				PacketSendUtility.sendPacket(player, STR_ITEM_IS_NOT_USABLE);
				return;
			}
			Npc targetNpc = (Npc)player.getTarget();
			if (targetNpc.getRace() != item.getItemTemplate().getActivationRace()) {
				PacketSendUtility.sendPacket(player, STR_SKILL_CANT_CAST_TO_CURRENT_TARGET);
				return;
			}
		}
		HandlerResult result = QuestEngine.getInstance().onItemUseEvent(new QuestEnv(null, player, 0, 0), item);
		if (result == HandlerResult.FAILED)
			return; // don't remove item

		ItemActions itemActions = item.getItemTemplate().getActions();
		ArrayList<AbstractItemAction> actions = new ArrayList<AbstractItemAction>();

		if (itemActions == null) {
			PacketSendUtility.sendPacket(player, STR_ITEM_IS_NOT_USABLE);
			return;
		}
		//for multi-return scrolls
		item.setIndexReturn(indexReturn);

		for (AbstractItemAction itemAction : itemActions.getItemActions()) {
			// check if the item can be used before placing it on the cooldown list.
			if (targetHouseObject != null && itemAction instanceof IHouseObjectDyeAction) {
				IHouseObjectDyeAction action = (IHouseObjectDyeAction) itemAction;
				if (action != null && action.canAct(player, item, targetHouseObject))
					actions.add(itemAction);
			}
			else if (itemAction.canAct(player, item, targetItem))
				actions.add(itemAction);
		}

		if (actions.size() == 0) {
			PacketSendUtility.sendPacket(player, STR_ITEM_IS_NOT_USABLE);
			return;
		}

		// Store Item CD in server Player variable.
		// Prevents potion spamming, and relogging to use kisks/aether jelly/long CD items.
		if (player.isItemUseDisabled(item.getItemTemplate().getUseLimits())) {
			PacketSendUtility.sendPacket(player, STR_ITEM_CANT_USE_UNTIL_DELAY_TIME);
			return;
		}

		int useDelay = player.getItemCooldown(item.getItemTemplate());
		if (useDelay > 0) {
			player.addItemCoolDown(item.getItemTemplate().getUseLimits().getDelayId(), System.currentTimeMillis() + useDelay,
				useDelay / 1000);
		}

		// notify item use observer
		player.getObserveController().notifyItemuseObservers(item);

		for (AbstractItemAction itemAction : actions) {
			if (targetHouseObject != null && itemAction instanceof IHouseObjectDyeAction) {
				IHouseObjectDyeAction action = (IHouseObjectDyeAction) itemAction;
				action.act(player, item, targetHouseObject);
			}
			else {
				itemAction.act(player, item, targetItem);
			}
		}
	}
}
