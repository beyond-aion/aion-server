package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.HashSet;
import java.util.Set;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.pet.PetFunction;
import com.aionemu.gameserver.model.templates.pet.PetFunctionType;
import com.aionemu.gameserver.model.templates.tradelist.TradeListTemplate;
import com.aionemu.gameserver.model.templates.tradelist.TradeNpcType;
import com.aionemu.gameserver.model.trade.RepurchaseList;
import com.aionemu.gameserver.model.trade.TradeList;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.DialogService;
import com.aionemu.gameserver.services.PrivateStoreService;
import com.aionemu.gameserver.services.RepurchaseService;
import com.aionemu.gameserver.services.TradeService;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author orz, ATracer, Simple, xTz
 */
public class CM_BUY_ITEM extends AionClientPacket {

	private int sellerObjId;
	private TradeAction tradeAction;
	private boolean isAudit;
	private TradeList tradeList;
	private RepurchaseList repurchaseList;

	public CM_BUY_ITEM(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		Player player = getConnection().getActivePlayer();
		sellerObjId = readD();
		tradeAction = TradeAction.fromId(readUC());
		readC(); // unused
		int amount = readUH(); // total no of items

		if (tradeAction == null)
			return;
		if (amount > 36) {
			isAudit = true;
			AuditLogger.log(player, "might be abusing CM_BUY_ITEM amount: " + amount);
			return;
		}
		if (tradeAction == TradeAction.SHOP_REPURCHASE) {
			repurchaseList = new RepurchaseList(sellerObjId);
		} else {
			tradeList = new TradeList(sellerObjId);
		}

		Set<Integer> entryIds = new HashSet<>();
		for (int i = 0; i < amount; i++) {
			int entryId = readD(); // see TradeAction.entryType
			long count = readQ();

			// prevent exploit packets
			if (count <= 0 || (entryId <= 0 && tradeAction.entryType != EntryType.STORE_INDEX) || count > 20000) {
				isAudit = true;
				AuditLogger.log(player, "might be abusing CM_BUY_ITEM entry: " + tradeAction.entryType + " " + entryId + " was sent with count " + count + " for " + tradeAction);
				break;
			}
			if (tradeAction.entryType == EntryType.STORE_INDEX && entryId < 0) {
				isAudit = true;
				AuditLogger.log(player, "might be abusing CM_BUY_ITEM: " + tradeAction.entryType + " " + entryId + " was sent for " + tradeAction);
				break;
			}
			if ((tradeAction.entryType == EntryType.ITEM_OBJECT_ID || tradeAction.entryType == EntryType.STORE_INDEX) && !entryIds.add(entryId)) {
				isAudit = true;
				AuditLogger.log(player, "might be abusing CM_BUY_ITEM: " + tradeAction.entryType + " " + entryId + " was sent more than once for " + tradeAction);
				break;
			}

			if (tradeAction == TradeAction.SHOP_REPURCHASE)
				repurchaseList.addRepurchaseItem(player, entryId, count);
			else
				tradeList.addItem(entryId, count);
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (tradeAction == null || isAudit || player == null)
			return;

		switch (player.getKnownList().getObject(sellerObjId)) {
			case Player seller when tradeAction == TradeAction.PRIVATE_STORE_BUY -> PrivateStoreService.sellStoreItem(seller, player, tradeList);
			case Npc npc -> tradeWithNpc(player, npc);
			case Pet pet when pet.getMaster() == player && tradeAction == TradeAction.PET_SELL -> {
				PetFunction pf = pet.getObjectTemplate().getPetFunction(PetFunctionType.MERCHANT);
				if (pf != null)
					TradeService.performSellToShop(player, tradeList, null, pf.getRatePrice());
			}
			case null, default -> {
			}
		}
	}

	private void tradeWithNpc(Player player, Npc npc) {
		if (!DialogService.isInteractionAllowed(player, npc)) {
			AuditLogger.log(player, "might be abusing CM_BUY_ITEM: no right trading with " + npc);
			return;
		}
		switch (tradeAction) {
			case SHOP_SELL -> {
				if (npc.canBuy() || npc.canPurchase()) {
					TradeListTemplate tradeTemplate = DataManager.TRADE_LIST_DATA.getPurchaseTemplate(npc.getNpcId());
					if (tradeTemplate != null && tradeTemplate.getTradeNpcType() == TradeNpcType.ABYSS)
						TradeService.performSellForAPToShop(player, tradeList, tradeTemplate);
					else
						TradeService.performSellToShop(player, tradeList, tradeTemplate);
				}
			}
			case SHOP_REPURCHASE -> {
				if (npc.canBuy())
					RepurchaseService.getInstance().repurchaseFromShop(player, repurchaseList);
			}
			case SHOP_BUY, ABYSS_SHOP_BUY, REWARD_SHOP_BUY, ABYSS_KINAH_SHOP_BUY -> {
				if (npc.canSell())
					TradeService.buyFromShop(npc, player, tradeList);
			}
			default -> {
			}
		}
	}

	private enum TradeAction {

		PRIVATE_STORE_BUY(0, EntryType.STORE_INDEX),
		SHOP_SELL(1, EntryType.ITEM_OBJECT_ID),
		SHOP_REPURCHASE(2, EntryType.ITEM_OBJECT_ID),
		SHOP_BUY(13, EntryType.ITEM_ID), // the NPC's trade type decides the price, not which of the buy actions the client sends
		ABYSS_SHOP_BUY(14, EntryType.ITEM_ID),
		REWARD_SHOP_BUY(15, EntryType.ITEM_ID),
		ABYSS_KINAH_SHOP_BUY(16, EntryType.ITEM_ID),
		PET_SELL(17, EntryType.ITEM_OBJECT_ID);

		private final int id;
		private final EntryType entryType;

		TradeAction(int id, EntryType entryType) {
			this.id = id;
			this.entryType = entryType;
		}

		private static TradeAction fromId(int id) {
			for (TradeAction action : values())
				if (action.id == id)
					return action;
			return null;
		}
	}

	/**
	 * What the ID of each item entry in the packet refers to.
	 */
	private enum EntryType {
		STORE_INDEX, // index in the seller's private store list
		ITEM_ID,
		ITEM_OBJECT_ID
	}
}
