package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AntiHackConfig;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.actions.EnchantItemAction;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.EnchantService;
import com.aionemu.gameserver.services.item.ItemSocketService;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, Wakizashi
 *
 */
public class CM_MANASTONE extends AionClientPacket {

	private int npcObjId;
	private int slotNum;
	private int actionType;
	private int targetFusedSlot;
	private int stoneUniqueId;
	private int targetItemUniqueId;
	private int supplementUniqueId;

	/**
	 * @param opcode
	 */
	public CM_MANASTONE(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		actionType = readC();
		targetFusedSlot = readC();
		targetItemUniqueId = readD();
		switch (actionType) {
			case 1:
			case 2:
			case 4:
			case 8:
				stoneUniqueId = readD();
				supplementUniqueId = readD();
				break;
			case 3:
				slotNum = readC();
				readC();
				readH();
				npcObjId = readD();
				break;
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		VisibleObject obj = player.getKnownList().getObject(npcObjId);

		if(player.getPlayerAccount().isHacked() && !AntiHackConfig.HDD_SERIAL_HACKED_ACCOUNTS_ALLOW_MANASTONE_SOCKETING) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_L2AUTH_S_KICKED_DOUBLE_LOGIN);
			PacketSendUtility.sendMessage(player, "Account hacking attempt detected. You can't use this function. Please, contact your server support.");
			return;
		}
		
		switch (actionType) {
			case 1: // enchant stone
			case 2: // add manastone
				EnchantItemAction action = new EnchantItemAction();
				Item manastone = player.getInventory().getItemByObjId(stoneUniqueId);
				Item targetItem = player.getEquipment().getEquippedItemByObjId(targetItemUniqueId);
				if (targetItem == null) {
					targetItem = player.getInventory().getItemByObjId(targetItemUniqueId);
				}
				if (action.canAct(player, manastone, targetItem)) {
					Item supplement = player.getInventory().getItemByObjId(supplementUniqueId);
					if (supplement != null) {
						if (supplement.getItemId() / 100000 != 1661) { // suppliment id check
							return;
						}
					}
					action.act(player, manastone, targetItem, supplement, targetFusedSlot);
				}
				break;
			case 3: // remove manastone
				if (obj != null && obj instanceof Npc && MathUtil.isInRange(player, obj, 7)) {
					if (targetFusedSlot == 1)
						ItemSocketService.removeManastone(player, targetItemUniqueId, slotNum);
					else
						ItemSocketService.removeFusionstone(player, targetItemUniqueId, slotNum);
				}
				break;
			case 4: //add godstone
				ItemSocketService.socketGodstone(player, targetItemUniqueId, stoneUniqueId);
				break;
			case 8: // amplification
				EnchantService.amplifyItem(player, targetItemUniqueId, supplementUniqueId, stoneUniqueId);
				break;
		}
	}

}