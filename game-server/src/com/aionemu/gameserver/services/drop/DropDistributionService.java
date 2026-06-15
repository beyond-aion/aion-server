package com.aionemu.gameserver.services.drop;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.gameobjects.DropNpc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_LOOT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz, Sykra
 */
public class DropDistributionService {

	private static final Logger log = LoggerFactory.getLogger(DropDistributionService.class);

	public static DropDistributionService getInstance() {
		return SingletonHolder.instance;
	}

	public void handleRollOrBid(Player player, int mode, int roll, long bid, int itemId, int npcObjId, int index) {
		if (player == null)
			return;
		DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcObjId);
		if (dropNpc == null)
			return;
		Set<DropItem> dropItems = DropRegistrationService.getInstance().getCurrentDropMap().get(npcObjId);
		if (dropItems == null)
			return;
		DropItem requestedItem = null;
		synchronized (dropItems) {
			for (DropItem dropItem : dropItems)
				if (dropItem.getIndex() == dropNpc.getCurrentIndex()) {
					requestedItem = dropItem;
					break;
				}
		}
		if (requestedItem == null)
			return;
		if (mode == 2)
			handleRoll(player, roll, itemId, requestedItem, dropNpc);
		else if (mode == 3)
			handleBid(player, bid, itemId, requestedItem, dropNpc);
		else
			log.warn("{} requested invalid distributionMode {} for dropItem[itemId={}, index={}, npcObjId={}]", player, mode, itemId, index, npcObjId);
	}

	private void handleRoll(Player player, int roll, int itemId, DropItem requestedItem, DropNpc dropNpc) {
		int luck = 0;
		if (roll == 0) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DICE_GIVEUP_ME());
		} else {
			luck = Rnd.get(1, dropNpc.getMaxRoll());
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DICE_RESULT_ME(luck, dropNpc.getMaxRoll()));
		}
		for (Player member : dropNpc.getInRangePlayers()) {
			if (member == null) {
				log.warn("member null Owner is in group? " + player.isInGroup() + " Owner is in Alliance? " + player.isInAlliance());
				continue;
			}
			PacketSendUtility.sendPacket(member, new SM_GROUP_LOOT(dropNpc.getLootingTeamId(), member.getObjectId(), itemId, (int) requestedItem.getCount(),
				dropNpc.getObjectId(), dropNpc.getDistributionId(), luck, requestedItem.getIndex()));
			if (!player.equals(member) && member.isOnline()) {
				if (roll == 0) {
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_DICE_GIVEUP_OTHER(player.getName()));
				} else {
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_DICE_RESULT_OTHER(player.getName(), luck, dropNpc.getMaxRoll()));
				}
			}
		}
		distributeLoot(player, luck, itemId, requestedItem, dropNpc);
	}

	private void handleBid(Player player, long bid, int itemId, DropItem requestedItem, DropNpc dropNpc) {
		if ((bid > 0 && player.getInventory().getKinah() < bid) || bid < 0 || bid > 999999999)
			bid = 0;
		PacketSendUtility.sendPacket(player, bid > 0 ? SM_SYSTEM_MESSAGE.STR_MSG_PAY_RESULT_ME() : SM_SYSTEM_MESSAGE.STR_MSG_PAY_GIVEUP_ME());
		for (Player member : dropNpc.getInRangePlayers()) {
			PacketSendUtility.sendPacket(member, new SM_GROUP_LOOT(dropNpc.getLootingTeamId(), member.getObjectId(), itemId, (int) requestedItem.getCount(),
				dropNpc.getObjectId(), dropNpc.getDistributionId(), bid, requestedItem.getIndex()));
			if (!player.equals(member) && member.isOnline()) {
				if (bid > 0) {
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_PAY_RESULT_OTHER(player.getName()));
				} else {
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_PAY_GIVEUP_OTHER(player.getName()));
				}
			}
		}
		distributeLoot(player, bid, itemId, requestedItem, dropNpc);
	}

	private void distributeLoot(Player player, long luckyPlayer, int itemId, DropItem requestedItem, DropNpc dropNpc) {
		player.unsetPlayerMode(PlayerMode.IN_ROLL);
		// Removes player from ARRAY once they have rolled or bid
		if (dropNpc.containsPlayerStatus(player))
			dropNpc.delPlayerStatus(player);

		if (luckyPlayer > requestedItem.getHighestValue()) {
			requestedItem.setHighestValue(luckyPlayer);
			requestedItem.setWinningPlayer(player);
		}

		if (!dropNpc.getPlayerStatus().isEmpty())
			return;

		for (Player member : dropNpc.getInRangePlayers()) {
			if (member == null) {
				continue;
			}
			if (requestedItem.getWinningPlayer() == null) {
				PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_PAY_ALL_GIVEUP());
			}
			PacketSendUtility.sendPacket(member,
				new SM_GROUP_LOOT(dropNpc.getLootingTeamId(), requestedItem.getWinningPlayer() != null ? requestedItem.getWinningPlayer().getObjectId() : 1, itemId,
					(int) requestedItem.getCount(), dropNpc.getObjectId(), dropNpc.getDistributionId(), 0xFFFFFFFF, requestedItem.getIndex()));
		}

		LootGroupRules lgr = dropNpc.getLootGroupRules();
		if (lgr != null)
			lgr.removeItemToBeDistributed(requestedItem);

		// Check if there is a Winning Player registered if not all members must have passed...
		if (requestedItem.getWinningPlayer() == null) {
			requestedItem.isFreeForAll(true);
			if (lgr != null && !lgr.getItemsToBeDistributed().isEmpty())
				DropService.getInstance().canDistribute(player, lgr.getItemsToBeDistributed().getFirst());
			return;
		}

		requestedItem.isDistributeItem(true);
		DropService.getInstance().requestDropItem(player, dropNpc.getObjectId(), dropNpc.getCurrentIndex());
		if (lgr != null && !lgr.getItemsToBeDistributed().isEmpty())
			DropService.getInstance().canDistribute(player, lgr.getItemsToBeDistributed().getFirst());
	}

	private static class SingletonHolder {

		protected static final DropDistributionService instance = new DropDistributionService();
	}

}
