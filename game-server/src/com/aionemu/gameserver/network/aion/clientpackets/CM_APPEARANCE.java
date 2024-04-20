package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.dao.LegionDAO;
import com.aionemu.gameserver.dao.OldNamesDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.templates.item.actions.AbstractItemAction;
import com.aionemu.gameserver.model.templates.item.actions.CosmeticItemAction;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RENAME;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.NameRestrictionService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.World;

/**
 * @author xTz, Neon
 */
public class CM_APPEARANCE extends AionClientPacket {

	private byte type;
	private int itemObjId;
	private String newName;

	public CM_APPEARANCE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		type = readC();
		readC();
		readH();
		itemObjId = readD();
		switch (type) {
			case 0:
			case 1:
				newName = readS();
				break;
		}

	}

	@Override
	protected void runImpl() {
		final Player player = getConnection().getActivePlayer();

		switch (type) {
			case 0: // Change Char Name
				tryChangeCharacterName(player, Util.convertName(newName), itemObjId);
				break;
			case 1: // Change Legion Name
				tryChangeLegionName(player, newName, itemObjId);
				break;
			case 2: // cosmetic items
				tryUseCosmeticItem(player, itemObjId);
				break;
		}
	}

	private void tryChangeCharacterName(Player player, String newName, int itemObjId) {
		if (player.getName().equals(newName))
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_CHAR_NAME_ERROR_SAME_YOUR_NAME());
		else if (!NameRestrictionService.isValidName(newName) || NameRestrictionService.isForbidden(newName))
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_CHAR_NAME_ERROR_WRONG_INPUT());
		else if (!PlayerService.isFreeName(newName) || !CustomConfig.OLD_NAMES_COUPON_DISABLED && PlayerService.isOldName(newName))
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_CHAR_NAME_ALREADY_EXIST());
		else if ((player.getInventory().getItemByObjId(itemObjId).getItemId() != 169670000 && player.getInventory().getItemByObjId(itemObjId).getItemId() != 169670001)
			|| !player.getInventory().decreaseByObjectId(itemObjId, 1))
			AuditLogger.log(player, "tried to rename himself without coupon");
		else {
			String oldName = player.getName();
			if (!CustomConfig.OLD_NAMES_COUPON_DISABLED)
				DAOManager.getDAO(OldNamesDAO.class).insertNames(player.getObjectId(), oldName, newName);

			player.getCommonData().setName(newName);
			DAOManager.getDAO(PlayerDAO.class).storePlayer(player);
			onPlayerNameChanged(player, oldName);
		}
	}

	public static void onPlayerNameChanged(Player player, String oldName) {
		World.getInstance().updateCachedPlayerName(oldName, player);
		LegionService.getInstance().updateCachedPlayerName(oldName, player);
		PacketSendUtility.broadcastToWorld(new SM_RENAME(player, oldName)); // broadcast to world to update all friendlists, housing npcs, etc.
	}

	private void tryChangeLegionName(Player player, String newName, int itemObjId) {
		if (!player.isLegionMember() || !player.getLegionMember().isBrigadeGeneral())
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_GUILD_NAME_ERROR_ONLY_MASTER_CAN_CHANGE_NAME());
		else if (player.getLegion().getName().equals(newName))
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_GUILD_NAME_ERROR_SAME_YOUR_NAME());
		else if (!NameRestrictionService.isValidLegionName(newName) || NameRestrictionService.isForbidden(newName))
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_GUILD_NAME_ERROR_WRONG_INPUT());
		else if (DAOManager.getDAO(LegionDAO.class).isNameUsed(newName))
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_GUILD_NAME_ALREADY_EXIST());
		else if ((player.getInventory().getItemByObjId(itemObjId).getItemId() != 169680000 && player.getInventory().getItemByObjId(itemObjId).getItemId() != 169680001)
			|| !player.getInventory().decreaseByObjectId(itemObjId, 1))
			AuditLogger.log(player, "Tried to rename legion without coupon.");
		else {
			Legion legion = player.getLegion();

			String oldName = legion.getName();
			legion.setName(newName);
			DAOManager.getDAO(LegionDAO.class).storeLegion(legion);
			PacketSendUtility.broadcastToWorld(new SM_RENAME(legion, oldName)); // broadcast to world to update all keeps, member's tags, etc.
		}
	}

	private void tryUseCosmeticItem(Player player, int itemObjId) {
		Item item = player.getInventory().getItemByObjId(itemObjId);
		if (item != null) {
			for (AbstractItemAction action : item.getItemTemplate().getActions().getItemActions()) {
				if (action instanceof CosmeticItemAction && action.canAct(player, null, null)) {
					action.act(player, null, item);
					break;
				}
			}
		}
	}
}
