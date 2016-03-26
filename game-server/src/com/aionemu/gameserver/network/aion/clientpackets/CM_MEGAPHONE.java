package com.aionemu.gameserver.network.aion.clientpackets;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_CLASS;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_RACE;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_TOO_HIGH_LEVEL;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_TOO_LOW_LEVEL_MUST_BE_THIS_LEVEL;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_ITEM_CANT_USE_UNTIL_DELAY_TIME;
import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_ITEM_IS_NOT_USABLE;

import java.util.Iterator;
import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.actions.AbstractItemAction;
import com.aionemu.gameserver.model.templates.item.actions.ItemActions;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MEGAPHONE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Artur, ginho1
 */
public class CM_MEGAPHONE extends AionClientPacket {

	private String message;
	private int uniqueItemId;

	public CM_MEGAPHONE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		message = readS();
		uniqueItemId = readD();
	}

	@Override
	protected void runImpl() {

		Player player = getConnection().getActivePlayer();

		if (player.isProtectionActive()) {
			player.getController().stopProtectionActiveTask();
		}

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
			PacketSendUtility.sendPacket(player, STR_CANNOT_USE_ITEM_TOO_LOW_LEVEL_MUST_BE_THIS_LEVEL(item.getNameId(), requiredLevel));
			return;
		}

		byte levelRestrict = item.getItemTemplate().getMaxLevelRestrict(player.getPlayerClass());
		if (levelRestrict != 0 && player.getLevel() > levelRestrict) {
			PacketSendUtility.sendPacket(player, STR_CANNOT_USE_ITEM_TOO_HIGH_LEVEL(levelRestrict, item.getNameId()));
			return;
		}

		ItemActions itemActions = item.getItemTemplate().getActions();

		if (itemActions == null) {
			PacketSendUtility.sendPacket(player, STR_ITEM_IS_NOT_USABLE());
			return;
		}

		List<AbstractItemAction> actions = new FastTable<AbstractItemAction>();

		for (AbstractItemAction itemAction : itemActions.getItemActions()) {
			if (itemAction.canAct(player, item, null))
				actions.add(itemAction);
		}

		if (actions.size() == 0) {
			PacketSendUtility.sendPacket(player, STR_ITEM_IS_NOT_USABLE());
			return;
		}

		// Store Item CD in server Player variable.
		// Prevents potion spamming, and relogging to use.
		if (player.isItemUseDisabled(item.getItemTemplate().getUseLimits())) {
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

		Iterator<Player> iter = World.getInstance().getPlayersIterator();
		while (iter.hasNext()) {
			PacketSendUtility.sendPacket(iter.next(), new SM_MEGAPHONE(player.getName(), message, item.getItemId()));
		}
	}
}
