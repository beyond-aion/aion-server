package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
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

	private static final Logger log = LoggerFactory.getLogger(CM_BUY_ITEM.class);
	private int sellerObjId;
	private short tradeActionId;
	private int amount;
	private int itemId;
	private long count;
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
		tradeActionId = readH();
		amount = readUH(); // total no of items

		if (amount < 0 || amount > 36) {
			isAudit = true;
			AuditLogger.log(player, "might be abusing CM_BUY_ITEM amount: " + amount);
			return;
		}
		if (tradeActionId == 2) {
			repurchaseList = new RepurchaseList(sellerObjId);
		} else {
			tradeList = new TradeList(sellerObjId);
		}

		for (int i = 0; i < amount; i++) {
			itemId = readD();
			count = readQ();

			// prevent exploit packets
			if (count < 0 || (itemId <= 0 && tradeActionId != 0) || count > 20000) {
				isAudit = true;
				AuditLogger.log(player, "might be abusing CM_BUY_ITEM item: " + itemId + " count: " + count);
				break;
			}

			switch (tradeActionId) {
				case 0:// private store (in this case its not itemId/objId, but item index in sellers list...)
				case 1:// sell to shop
				case 13:// buy from shop
				case 14:// buy from abyss shop
				case 15:// buy from reward shop
				case 16:// buy from general shop
				case 17:// sell to pet
					tradeList.addItem(itemId, count);
					break;
				case 2:// repurchase
					repurchaseList.addRepurchaseItem(player, itemId, count);
					break;
			}
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();

		if (isAudit || player == null)
			return;

		VisibleObject target = player.getKnownList().getObject(sellerObjId);

		if (target == null)
			return;

		if (target instanceof Player targetPlayer && tradeActionId == 0) {
			PrivateStoreService.sellStoreItem(targetPlayer, player, tradeList);
		} else if (target instanceof Npc npc) {
			if (!DialogService.isInteractionAllowed(player, npc)) {
				AuditLogger.log(player, "might be abusing CM_BUY_ITEM: no right trading with " + npc);
				return;
			}
			TradeListTemplate tradeTemplate;
			switch (tradeActionId) {
				case 1: // sell to shop
					if (npc.canBuy() || npc.canPurchase()) {
						tradeTemplate = DataManager.TRADE_LIST_DATA.getPurchaseTemplate(npc.getNpcId());
						if (tradeTemplate != null && tradeTemplate.getTradeNpcType() == TradeNpcType.ABYSS)
							TradeService.performSellForAPToShop(player, tradeList, tradeTemplate);
						else
							TradeService.performSellToShop(player, tradeList, tradeTemplate);
					}
					break;
				case 2: // repurchase
					if (npc.canBuy())
						RepurchaseService.getInstance().repurchaseFromShop(player, repurchaseList);
					break;
				case 13: // buy from shop
				case 14: // buy from abyss shop
				case 15: // reward shop
				case 16: // abyss_kinah shop
					if (npc.canSell())
						TradeService.performBuyFromShop(npc, player, tradeList);
					break;
				default:
					log.warn("Unknown shop action: " + tradeActionId);
					break;
			}
		} else if (target instanceof Pet) {
			PetFunction pf = ((Pet) target).getObjectTemplate().getPetFunction(PetFunctionType.MERCHANT);
			if (pf != null && tradeActionId == 17) {
				TradeService.performSellToShop(player, tradeList, null, pf.getRatePrice());
			}
		}
	}
}
