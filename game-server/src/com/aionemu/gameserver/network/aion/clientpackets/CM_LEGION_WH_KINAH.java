package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.team.legion.LegionHistoryAction;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.model.team.legion.LegionPermissionsMask;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.item.ItemRestrictionService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class CM_LEGION_WH_KINAH extends AionClientPacket {

	private long amount;
	private Action action;

	public CM_LEGION_WH_KINAH(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		amount = readQ();
		action = Action.getById(readUC());
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();
		LegionMember legionMember = activePlayer.getLegionMember();
		if (action == null || legionMember == null || !ItemRestrictionService.isLegionWarehouseUser(activePlayer))
			return;
		switch (action) {
			case WITHDRAW -> {
				if (!legionMember.hasRights(LegionPermissionsMask.WH_WITHDRAWAL)) {
					// You do not have the authority to use the Legion warehouse.
					PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_WAREHOUSE_NO_RIGHT());
					return;
				}
				if (activePlayer.getStorage(StorageType.LEGION_WAREHOUSE.getId()).tryDecreaseKinah(amount)) {
					activePlayer.getInventory().increaseKinah(amount);
					LegionService.getInstance().addHistory(legionMember.getLegion(), activePlayer.getName(), LegionHistoryAction.KINAH_WITHDRAW, Long.toString(amount));
				}
			}
			case DEPOSIT -> {
				if (!legionMember.hasRights(LegionPermissionsMask.WH_DEPOSIT)) {
					// You do not have the authority to use the Legion warehouse.
					PacketSendUtility.sendPacket(activePlayer, SM_SYSTEM_MESSAGE.STR_GUILD_WAREHOUSE_NO_RIGHT());
					return;
				}
				if (activePlayer.getInventory().tryDecreaseKinah(amount)) {
					activePlayer.getStorage(StorageType.LEGION_WAREHOUSE.getId()).increaseKinah(amount);
					LegionService.getInstance().addHistory(legionMember.getLegion(), activePlayer.getName(), LegionHistoryAction.KINAH_DEPOSIT, Long.toString(amount));
				}
			}
		}
	}

	private enum Action {

		WITHDRAW(0),
		DEPOSIT(1);

		private final int id;

		Action(int id) {
			this.id = id;
		}

		static Action getById(int id) {
			for (Action value : values())
				if (value.id == id)
					return value;
			return null;
		}
	}
}
