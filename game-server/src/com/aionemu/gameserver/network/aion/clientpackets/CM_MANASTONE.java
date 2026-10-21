package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.actions.EnchantItemAction;
import com.aionemu.gameserver.model.templates.item.actions.ItemActions;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.restrictions.PlayerRestrictions;
import com.aionemu.gameserver.services.EnchantService;
import com.aionemu.gameserver.services.StigmaService;
import com.aionemu.gameserver.services.item.ItemSocketService;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author ATracer, Wakizashi
 */
public class CM_MANASTONE extends AionClientPacket {

	private Action action;
	private int targetFusedSlot;
	private int targetItemUniqueId;
	private int stoneUniqueId;
	private int supplementUniqueId;
	private int slotNum;
	private int npcObjId;

	public CM_MANASTONE(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		action = Action.fromId(readUC());
		targetFusedSlot = readUC();
		targetItemUniqueId = readD();
		if (action == Action.REMOVE_MANASTONE) {
			slotNum = readD();
			npcObjId = readD();
		} else {
			stoneUniqueId = readD();
			supplementUniqueId = readD();
		}
	}

	@Override
	protected void runImpl() {
		if (action == null)
			return;
		Player player = getConnection().getActivePlayer();

		player.getController().cancelCurrentSkill(null);
		player.getController().cancelUseItem();

		switch (action) {
			case ENCHANT, SOCKET_MANASTONE -> {
				Item stone = player.getInventory().getItemByObjId(stoneUniqueId);
				if (stone == null)
					return;
				if (!stone.getItemTemplate().isStigma() && !PlayerRestrictions.canUseItem(player, stone))
					return;
				Item targetItem = player.getEquipment().getEquippedItemByObjId(targetItemUniqueId);
				if (targetItem == null && (targetItem = player.getInventory().getItemByObjId(targetItemUniqueId)) == null) {
					sendPacket(action == Action.ENCHANT ? SM_SYSTEM_MESSAGE.STR_ENCHANT_ITEM_NO_TARGET_ITEM() : SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_OPTION_NO_TARGET_ITEM());
					return;
				}

				if (stone.getItemTemplate().isStigma() && targetItem.getItemTemplate().isStigma()) {
					StigmaService.chargeStigma(player, targetItem, stone);
				} else {
					ItemActions stoneActions = stone.getItemTemplate().getActions();
					EnchantItemAction enchantAction = stoneActions == null ? null : stoneActions.getEnchantAction();
					if (enchantAction == null) {
						sendPacket(SM_SYSTEM_MESSAGE.STR_ITEM_IS_NOT_USABLE());
						return;
					}
					if (enchantAction.canAct(player, stone, targetItem)) {
						Item supplement = null;
						if (supplementUniqueId != 0) {
							supplement = player.getInventory().getItemByObjId(supplementUniqueId);
							if (supplement == null || supplement.getItemId() / 100000 != 1661) {
								sendPacket(SM_SYSTEM_MESSAGE.STR_ITEM_ENCHANT_ASSISTANT_CANNOT_FIND());
								return;
							}
						}
						enchantAction.act(player, stone, targetItem, supplement, targetFusedSlot);
					}
				}
			}
			case REMOVE_MANASTONE -> {
				if (player.getTarget() instanceof Npc npc && npc.getObjectId() == npcObjId && PositionUtil.isInTalkRange(player, npc))
					ItemSocketService.removeManastone(player, targetItemUniqueId, slotNum, targetFusedSlot != 1);
			}
			case SOCKET_GODSTONE -> {
				Item weaponItem = player.getInventory().getItemByObjId(targetItemUniqueId);
				if (weaponItem == null) {
					boolean isEquipped = player.getEquipment().getEquippedItemByObjId(targetItemUniqueId) != null;
					sendPacket(isEquipped ? SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_CANNOT_GIVE_PROC_TO_EQUIPPED_ITEM() : SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_PROC_NO_TARGET_ITEM());
					return;
				}
				ItemSocketService.socketGodstone(player, weaponItem, stoneUniqueId);
			}
			case AMPLIFY -> EnchantService.amplifyItem(player, targetItemUniqueId, supplementUniqueId, stoneUniqueId);
		}
	}

	private enum Action {

		ENCHANT(1),
		SOCKET_MANASTONE(2),
		REMOVE_MANASTONE(3),
		SOCKET_GODSTONE(4),
		AMPLIFY(8);

		private final int id;

		Action(int id) {
			this.id = id;
		}

		static Action fromId(int id) {
			for (Action action : values())
				if (action.id == id)
					return action;
			return null;
		}
	}
}
