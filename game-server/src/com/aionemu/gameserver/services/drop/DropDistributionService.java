package com.aionemu.gameserver.services.drop;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.gameobjects.DropNpc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_LOOT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;

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
		// The players left to roll, the item's highest value and its winner are shared by all looters of this corpse, whose packets run on their own
		// threads: a roll and a bid at the same time could overwrite each other and hand the item out twice.
		boolean distributed = false;
		synchronized (dropNpc) {
			if (!dropNpc.containsPlayerStatus(player)) // only players the item was offered to may roll or bid (also covers a repeated roll)
				return;
			if (index != dropNpc.getCurrentIndex()) // a late answer for an item that was already distributed
				return;
			DropItem requestedItem = null;
			synchronized (dropItems) {
				for (DropItem dropItem : dropItems)
					if (dropItem.getIndex() == index) {
						requestedItem = dropItem;
						break;
					}
			}
			if (requestedItem == null)
				return;
			itemId = requestedItem.getDropTemplate().getItemId(); // the packet's id may not match the item the server keeps at this index
			if (mode == 2)
				distributed = handleRoll(player, roll, itemId, requestedItem, dropNpc);
			else if (mode == 3)
				distributed = handleBid(player, bid, itemId, requestedItem, dropNpc);
			else
				log.warn("{} requested invalid distributionMode {} for dropItem[itemId={}, index={}, npcObjId={}]", player, mode, itemId, index,
					npcObjId);
		}
		// outside the lock, since the next item may be of another corpse, which is locked to offer it
		if (distributed)
			DropService.getInstance().offerNextItem(player, dropNpc.getLootGroupRules());
	}

	private boolean handleRoll(Player player, int roll, int itemId, DropItem requestedItem, DropNpc dropNpc) {
		int luck = roll == 0 ? 0 : Rnd.get(1, dropNpc.getMaxRoll());
		requestedItem.setAnswer(player.getObjectId(), luck);
		for (Player member : offeredPlayers(requestedItem)) {
			PacketSendUtility.sendPacket(member, new SM_GROUP_LOOT(dropNpc.getLootingTeamId(), player.getObjectId(), itemId, (int) requestedItem.getCount(),
				dropNpc.getObjectId(), dropNpc.getDistributionId(), luck, requestedItem.getIndex()));
			if (member.equals(player))
				PacketSendUtility.sendPacket(member,
					luck == 0 ? SM_SYSTEM_MESSAGE.STR_MSG_DICE_GIVEUP_ME() : SM_SYSTEM_MESSAGE.STR_MSG_DICE_RESULT_ME(luck, dropNpc.getMaxRoll()));
			else
				PacketSendUtility.sendPacket(member, luck == 0 ? SM_SYSTEM_MESSAGE.STR_MSG_DICE_GIVEUP_OTHER(player.getName())
					: SM_SYSTEM_MESSAGE.STR_MSG_DICE_RESULT_OTHER(player.getName(), luck, dropNpc.getMaxRoll()));
		}
		return distributeLoot(player, luck, itemId, requestedItem, dropNpc);
	}

	private boolean handleBid(Player player, long bid, int itemId, DropItem requestedItem, DropNpc dropNpc) {
		if (bid < 0 || bid > 999999999)
			bid = 0; // whether the bidder can pay is checked when the bids end, see settleBids
		requestedItem.setAnswer(player.getObjectId(), bid);
		for (Player member : offeredPlayers(requestedItem)) {
			PacketSendUtility.sendPacket(member, new SM_GROUP_LOOT(dropNpc.getLootingTeamId(), player.getObjectId(), itemId, (int) requestedItem.getCount(),
				dropNpc.getObjectId(), dropNpc.getDistributionId(), bid, requestedItem.getIndex()));
			if (member.equals(player))
				PacketSendUtility.sendPacket(member, bid > 0 ? SM_SYSTEM_MESSAGE.STR_MSG_PAY_RESULT_ME() : SM_SYSTEM_MESSAGE.STR_MSG_PAY_GIVEUP_ME());
			else
				PacketSendUtility.sendPacket(member,
					bid > 0 ? SM_SYSTEM_MESSAGE.STR_MSG_PAY_RESULT_OTHER(player.getName()) : SM_SYSTEM_MESSAGE.STR_MSG_PAY_GIVEUP_OTHER(player.getName()));
		}
		return distributeLoot(player, bid, itemId, requestedItem, dropNpc);
	}

	/**
	 * @return True if everyone has rolled or bid, so the item is distributed and the next one in the team's queue can be offered.
	 */
	private boolean distributeLoot(Player player, long value, int itemId, DropItem requestedItem, DropNpc dropNpc) {
		if (dropNpc.containsPlayerStatus(player))
			dropNpc.delPlayerStatus(player);

		if (dropNpc.getDistributionId() != 3 && value > requestedItem.getHighestValue()) {
			requestedItem.setHighestValue(value);
			requestedItem.setWinningPlayer(player);
		}

		if (!dropNpc.getPlayerStatus().isEmpty())
			return false;
		List<Player> offeredPlayers = offeredPlayers(requestedItem);
		if (dropNpc.getDistributionId() == 3)
			settleBids(requestedItem, offeredPlayers);
		else
			announceRollWinner(requestedItem, offeredPlayers);

		Player winner = requestedItem.getWinningPlayer();
		for (Player member : offeredPlayers)
			PacketSendUtility.sendPacket(member, new SM_GROUP_LOOT(dropNpc.getLootingTeamId(), winner != null ? winner.getObjectId() : 1, itemId,
				(int) requestedItem.getCount(), dropNpc.getObjectId(), dropNpc.getDistributionId(), 0xFFFFFFFF, requestedItem.getIndex()));

		LootGroupRules lgr = dropNpc.getLootGroupRules();
		if (lgr != null)
			lgr.removeItemToBeDistributed(requestedItem);

		if (winner == null) {
			requestedItem.isFreeForAll(true);
			return true;
		}

		requestedItem.isDistributeItem(true);
		// a winner nearby gets the item right away, anyone else has to come and pick it up
		VisibleObject corpse = World.getInstance().findVisibleObject(dropNpc.getObjectId());
		if (winner.isOnline() && corpse != null && PositionUtil.isInRange(winner, corpse, GroupConfig.GROUP_MAX_DISTANCE))
			DropService.getInstance().requestDropItem(winner, dropNpc.getObjectId(), requestedItem.getIndex());
		else
			requestedItem.isItemWonNotCollected(true);
		return true;
	}

	/**
	 * Tells everyone who could roll who won the roll, compared with their own roll.
	 */
	private void announceRollWinner(DropItem requestedItem, List<Player> offeredPlayers) {
		Player winner = requestedItem.getWinningPlayer();
		if (winner == null) {
			offeredPlayers.forEach(p -> PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_MSG_GET_ITEM_PARTYNOTICE_DICE_GIVEUP_ROLL_ALL()));
			return;
		}
		String winnerRoll = String.valueOf(requestedItem.getHighestValue());
		for (Player member : offeredPlayers) {
			long ownRoll = requestedItem.getAnswer(member.getObjectId());
			SM_SYSTEM_MESSAGE msg;
			if (member.getObjectId() == winner.getObjectId())
				msg = SM_SYSTEM_MESSAGE.STR_MSG_GET_ITEM_PARTYNOTICE_DICE_WIN();
			else if (ownRoll == 0)
				msg = winner.isOnline() ? SM_SYSTEM_MESSAGE.STR_MSG_GET_ITEM_PARTYNOTICE_DICE_GIVEUP_ROLL(winner.getName(), winnerRoll)
					: SM_SYSTEM_MESSAGE.STR_MSG_GET_ITEM_PARTYNOTICE_DICE_GIVEUP_ROLL_OFFLINE_WINNER(winnerRoll);
			else
				msg = winner.isOnline() ? SM_SYSTEM_MESSAGE.STR_MSG_GET_ITEM_PARTYNOTICE_DICE(winner.getName(), winnerRoll, String.valueOf(ownRoll))
					: SM_SYSTEM_MESSAGE.STR_MSG_GET_ITEM_PARTYNOTICE_DICE_OFFLINE_WINNER(winnerRoll, String.valueOf(ownRoll));
			PacketSendUtility.sendPacket(member, msg);
		}
	}

	/**
	 * Picks the winner among the bidders, highest first, skipping those who no longer have the kinah; the money is taken only from the player who
	 * actually gets the item.
	 */
	private void settleBids(DropItem requestedItem, List<Player> offeredPlayers) {
		for (Map.Entry<Integer, Long> bid : requestedItem.getAnswers()) {
			long kinah = bid.getValue();
			if (kinah <= 0)
				continue;
			Player bidder = World.getInstance().getPlayer(bid.getKey());
			if (bidder == null || !bidder.isOnline()) // a bidder who left can't pay from an inventory that was already saved
				continue;
			if (DropService.getInstance().payWinningBid(bidder, requestedItem, kinah)) {
				requestedItem.setWinningPlayer(bidder);
				return;
			}
		}
		requestedItem.setWinningPlayer(null);
		offeredPlayers.forEach(p -> PacketSendUtility.sendPacket(p, SM_SYSTEM_MESSAGE.STR_MSG_PAY_ALL_GIVEUP()));
	}

	/**
	 * @return The players the item was offered to who are still in the game.
	 */
	private List<Player> offeredPlayers(DropItem requestedItem) {
		return requestedItem.getOfferedTo().stream().map(id -> World.getInstance().getPlayer(id)).filter(p -> p != null && p.isOnline()).toList();
	}

	private static class SingletonHolder {

		protected static final DropDistributionService instance = new DropDistributionService();
	}

}
