package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.dao.OldNamesDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionHistoryAction;
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

	private Service service;
	private int itemObjId;
	private String newName;

	public CM_APPEARANCE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		service = Service.fromId(readD());
		itemObjId = readD();
		if (service == Service.RENAME_CHARACTER || service == Service.RENAME_LEGION)
			newName = readS();
	}

	@Override
	protected void runImpl() {
		if (service == null)
			return;
		final Player player = getConnection().getActivePlayer();

		switch (service) {
			case RENAME_CHARACTER -> tryChangeCharacterName(player, Util.convertName(newName), itemObjId);
			case RENAME_LEGION -> tryChangeLegionName(player, newName, itemObjId);
			case COSMETIC -> tryUseCosmeticItem(player, itemObjId);
		}
	}

	private void tryChangeCharacterName(Player player, String newName, int itemObjId) {
		String oldName = player.getName();
		if (oldName.equals(newName))
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_CHAR_NAME_ERROR_SAME_YOUR_NAME());
		else if (!NameRestrictionService.isValidName(newName) || NameRestrictionService.isForbidden(newName))
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_CHAR_NAME_ERROR_WRONG_INPUT());
		else if (PlayerService.isNameUsedOrReserved(oldName, newName))
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_CHAR_NAME_ALREADY_EXIST());
		else if (!isRenameCoupon(player.getInventory().getItemByObjId(itemObjId)) || !player.getInventory().decreaseByObjectId(itemObjId, 1))
			AuditLogger.log(player, "tried to rename himself without coupon");
		else {
			OldNamesDAO.insertNames(player.getObjectId(), oldName, newName);

			player.getCommonData().setName(newName);
			PlayerDAO.storePlayer(player);
			onPlayerNameChanged(player, oldName);
		}
	}

	private static boolean isRenameCoupon(Item item) {
		return item != null && (item.getItemId() == 169670000 || item.getItemId() == 169670001);
	}

	public static void onPlayerNameChanged(Player player, String oldName) {
		World.getInstance().updateCachedPlayerName(oldName, player);
		if (player.isLegionMember()) {
			LegionService.getInstance().addHistory(player.getLegion(), oldName, LegionHistoryAction.CHARACTER_RENAME, player.getName());
			player.getLegionMember().setPlayerData(player); // no need to broadcast SM_LEGION_UPDATE_MEMBER here, since SM_RENAME already handles it
		}
		PacketSendUtility.broadcastToWorld(new SM_RENAME(player, oldName)); // broadcast to world to update all friendlists, housing npcs, etc.
	}

	private void tryChangeLegionName(Player player, String newName, int itemObjId) {
		Legion legion = player.getLegion();
		if (legion == null || !player.getLegionMember().isBrigadeGeneral()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EDIT_GUILD_NAME_ERROR_ONLY_MASTER_CAN_CHANGE_NAME());
			return;
		}
		LegionService.getInstance().tryRename(legion, newName, player, itemObjId);
	}

	private void tryUseCosmeticItem(Player player, int itemObjId) {
		Item item = player.getInventory().getItemByObjId(itemObjId);
		if (item != null) {
			for (AbstractItemAction action : item.getItemTemplate().getActions().getItemActions()) {
				if (action instanceof CosmeticItemAction && action.canAct(player, null, null)) {
					action.act(player, item, null);
					break;
				}
			}
		}
	}

	private enum Service {

		RENAME_CHARACTER(0),
		RENAME_LEGION(1),
		COSMETIC(2);

		private final int id;

		Service(int id) {
			this.id = id;
		}

		static Service fromId(int id) {
			for (Service service : values())
				if (service.id == id)
					return service;
			return null;
		}
	}
}
