package com.aionemu.gameserver.services.drop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.DropConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.gameobjects.*;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.items.ItemId;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team.common.legacy.LootRuleType;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOT_STATUS.Status;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.services.toypet.PetService;
import com.aionemu.gameserver.taskmanager.tasks.TemporaryTradeTimeTask;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer, xTz
 */
public class DropService {

	private static final Logger log = LoggerFactory.getLogger(DropService.class);

	private static final int ROLL_TIMEOUT_MILLIS = 20000;
	private static final int BID_TIMEOUT_MILLIS = 35000;

	public static DropService getInstance() {
		return SingletonHolder.instance;
	}

	public void scheduleFreeForAll(final int npcUniqueId) {
		ThreadPoolManager.getInstance().schedule(() -> {
			DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcUniqueId);
			if (dropNpc != null) {
				DropRegistrationService.getInstance().getDropRegistrationMap().get(npcUniqueId).startFreeForAll();
				VisibleObject visibleObject = World.getInstance().findVisibleObject(npcUniqueId);
				if (visibleObject != null && visibleObject.isSpawned()) {
					// fix for elyos/asmodians being able to loot elyos/asmodian npcs
					// TODO there might be more npcs who are friendly towards players and should not be loot able by them
					if (visibleObject instanceof Npc npc && npc.getRace().isAsmoOrEly()) {
						PacketSendUtility.broadcastPacket(npc, new SM_LOOT_STATUS(npcUniqueId, Status.LOOT_ENABLE), p -> npc.getRace() != p.getRace());
					} else {
						PacketSendUtility.broadcastPacket(visibleObject, new SM_LOOT_STATUS(npcUniqueId, Status.LOOT_ENABLE));
					}
				}
			}
		}, 240000);
	}

	/**
	 * After NPC despawns
	 *
	 * @param npc
	 */
	public void unregisterDrop(Npc npc) {
		int npcObjId = npc.getObjectId();
		DropRegistrationService.getInstance().getCurrentDropMap().remove(npcObjId);
		DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().remove(npcObjId);
		if (dropNpc != null && dropNpc.getLootGroupRules() != null)
			removeFromDistribution(dropNpc, dropNpc.getLootGroupRules());
	}

	/**
	 * Removes the items of a corpse that is gone from its team's queue of items to roll or bid for. Only the first item of the queue is distributed,
	 * and only a roll or bid on it starts the next one, so the team could never roll again once a corpse decayed with its item in the queue.
	 */
	private void removeFromDistribution(DropNpc dropNpc, LootGroupRules lootGroupRules) {
		int npcObjId = dropNpc.getObjectId();
		DropItem first;
		synchronized (dropNpc) { // see DropDistributionService.handleRollOrBid
			synchronized (lootGroupRules.getItemsToBeDistributed()) {
				first = lootGroupRules.getItemsToBeDistributed().peekFirst();
				if (!lootGroupRules.getItemsToBeDistributed().removeIf(item -> item.getNpcObj() == npcObjId))
					return;
			}
		}
		DropItem next = lootGroupRules.getItemsToBeDistributed().peekFirst();
		if (next != null && next != first)
			offerNextItem(null, lootGroupRules);
	}

	/**
	 * When player clicks on dead NPC to request drop list
	 *
	 * @param player
	 * @param npcObjectId
	 */
	public void requestDropList(Player player, int npcObjectId) {
		DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcObjectId);
		if (player == null || dropNpc == null) {
			return;
		}

		if (player.isLooting())
			closeDropList(player, player.getLootingNpcOid());

		if (!dropNpc.isAllowedToLoot(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_LOOT_NO_RIGHT());
			return;
		}

		synchronized (dropNpc) {
			if (dropNpc.isBeingLooted()) {
				if (!dropNpc.getLootingPlayer().isOnline()) {
					log.warn("{} is offline but was still set as drop looter for {}", dropNpc.getLootingPlayer(),
						World.getInstance().findVisibleObject(npcObjectId));
				} else {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_LOOT_FAIL_ONLOOTING());
					return;
				}
			}
			dropNpc.setLootingPlayer(player);
		}
		VisibleObject visObj = World.getInstance().findVisibleObject(npcObjectId);
		if (visObj instanceof Npc npc) {
			ScheduledFuture<?> decayTask = (ScheduledFuture<?>) npc.getController().cancelTask(TaskId.DECAY);
			if (decayTask != null) {
				long remaingDecayTime = decayTask.getDelay(TimeUnit.MILLISECONDS);
				dropNpc.setRemaingDecayTime(remaingDecayTime);
			}
		}

		Set<DropItem> dropItems = DropRegistrationService.getInstance().getCurrentDropMap().get(npcObjectId);

		if (dropItems == null) {
			dropItems = Collections.emptySet();
		}

		PacketSendUtility.sendPacket(player, new SM_LOOT_ITEMLIST(dropNpc, dropItems, player));
		PacketSendUtility.sendPacket(player, new SM_LOOT_STATUS(npcObjectId, Status.OPEN_DROP_LIST));
		player.getController().cancelUseItem();
		player.unsetState(CreatureState.ACTIVE);
		player.setState(CreatureState.LOOTING);
		player.setLootingNpcOid(npcObjectId);
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_LOOT, 0, npcObjectId), true);
	}

	/**
	 * This method will change looted corpse to not in use
	 *
	 * @param player
	 * @param npcObjectId
	 */
	public void closeDropList(Player player, int npcObjectId) {
		DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcObjectId);

		player.unsetState(CreatureState.LOOTING);
		player.setState(CreatureState.ACTIVE);
		player.setLootingNpcOid(0);
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_LOOT, 0, npcObjectId), true);

		if (dropNpc == null)
			return;

		synchronized (dropNpc) {
			if (!player.equals(dropNpc.getLootingPlayer()))
				return;// cheater :)
			dropNpc.setLootingPlayer(null);
		}

		Set<DropItem> dropItems = DropRegistrationService.getInstance().getCurrentDropMap().get(npcObjectId);

		Npc npc = (Npc) World.getInstance().findVisibleObject(npcObjectId);
		if (npc == null)
			return;
		if (dropItems == null || dropItems.isEmpty()) {
			npc.getController().delete();
			return;
		}

		// Who may loot from now on and which drops belong to everyone is changed by this player's thread while another one may already take an item
		synchronized (dropNpc) {
			// another player may have opened the corpse since the lock was released above, and an open loot window keeps it from decaying
			if (!dropNpc.isBeingLooted())
				RespawnService.scheduleDecayTask(npc, dropNpc.getRemaingDecayTime());

			LootGroupRules lootGroupRules = dropNpc.getLootGroupRules();
			if (lootGroupRules != null && dropNpc.getInRangePlayers().size() > 1 && dropNpc.getAllowedLooters().size() == 1) {
				LootRuleType lrt = lootGroupRules.getLootRule();
				if (lrt != LootRuleType.FREEFORALL) {
					for (Player member : dropNpc.getInRangePlayers()) {
						if (member != null)
							dropNpc.setAllowedLooter(member);
					}
					for (DropItem dropItem : dropItems) {
						if (!dropItem.getDropTemplate().isEachMember())
							dropItem.getPlayerObjIds().clear();
					}
				}
			}
		}
		PacketSendUtility.broadcastPacket(npc, new SM_LOOT_STATUS(npcObjectId, Status.LOOT_ENABLE), dropNpc::isAllowedToLoot);
	}

	/**
	 * Offers the first item of the team's queue, if there is one. An item that nobody near its corpse is online for leaves the queue instead, and the
	 * one behind it is offered.
	 *
	 * @param player
	 *          The player whose answer ended the previous roll, or null.
	 */
	void offerNextItem(Player player, LootGroupRules lootGroupRules) {
		if (lootGroupRules == null)
			return;
		DropItem next;
		while ((next = lootGroupRules.getItemsToBeDistributed().peekFirst()) != null) { // a corpse that decays meanwhile takes its items out of the queue
			canDistribute(player, next);
			if (lootGroupRules.getItemsToBeDistributed().peekFirst() == next) // offered, or its corpse is gone and takes it out when it decays
				break;
		}
	}

	public boolean canDistribute(Player player, DropItem requestedItem) {
		int npcId = requestedItem.getNpcObj();
		final DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcId);
		if (dropNpc == null) {
			return false;
		}
		// The players who are offered the item are added to the corpse's roll state one after another, while the first of them may answer already,
		// on their own threads: an answer before the others were added would find nobody else left to roll and hand out the item.
		synchronized (dropNpc) {
			LootGroupRules lootGroupRules = dropNpc.getLootGroupRules();
			if (lootGroupRules == null)
				return true;
			// The queue is the team's: two items of different corpses could both find it empty and start two rolls at once, or one could be queued
			// right after the roll before it ended and found nothing to offer next, so it would never be offered.
			synchronized (lootGroupRules.getItemsToBeDistributed()) {
				return canDistribute(player, requestedItem, npcId, dropNpc, lootGroupRules);
			}
		}
	}

	private boolean canDistribute(Player player, DropItem requestedItem, int npcId, DropNpc dropNpc, LootGroupRules lootGroupRules) {
		// offered already: both a finished roll and a decayed corpse offer the next item of the queue
		if (requestedItem.getIndex() == dropNpc.getCurrentIndex() && !dropNpc.getPlayerStatus().isEmpty()
			&& lootGroupRules.containDropItem(requestedItem))
			return false;

		int itemId = requestedItem.getDropTemplate().getItemId();
		if (itemId != ItemId.KINAH) {
			if (dropNpc.getInRangePlayers().size() > 1) {
				ItemQuality quality = DataManager.ITEM_DATA.getItemTemplate(itemId).getItemQuality();
				dropNpc.setDistributionId(lootGroupRules.getAutodistributionId());
				dropNpc.setDistributionType(lootGroupRules.getQualityRule(quality));
			} else
				dropNpc.setDistributionId(0);
			if (dropNpc.getDistributionId() > 1 && dropNpc.getDistributionType()) {
				boolean containDropItem = lootGroupRules.containDropItem(requestedItem);
				if (lootGroupRules.getItemsToBeDistributed().isEmpty() || containDropItem) {
					int distributionId = dropNpc.getDistributionId();
					dropNpc.setCurrentIndex(requestedItem.getIndex());
					List<Integer> offeredTo = new ArrayList<>();
					for (Player member : dropNpc.getInRangePlayers()) {
						Player finalPlayer = World.getInstance().getPlayer(member.getObjectId());
						if (finalPlayer != null && finalPlayer.isOnline()) {
							offeredTo.add(finalPlayer.getObjectId());
							dropNpc.addPlayerStatus(finalPlayer);
							PacketSendUtility.sendPacket(finalPlayer, new SM_GROUP_LOOT(dropNpc.getLootingTeamId(), 0, itemId, (int) requestedItem.getCount(),
								npcId, distributionId, 1, requestedItem.getIndex()));
						}
					}
					if (offeredTo.isEmpty()) { // nobody would answer, so the roll would never end and the team's queue would wait for it
						lootGroupRules.removeItemToBeDistributed(requestedItem);
						return false;
					}
					requestedItem.offerTo(offeredTo, distributionId);
					lootGroupRules.scheduleRollTimeout(dropNpc.getInRangePlayers(), distributionId == 2 ? ROLL_TIMEOUT_MILLIS : BID_TIMEOUT_MILLIS,
						requestedItem.getIndex(), npcId, distributionId);
				} else if (player != null) {
					PacketSendUtility.sendPacket(player,
						SM_SYSTEM_MESSAGE.STR_MSG_LOOT_ALREADY_DISTRIBUTING_ITEM(DataManager.ITEM_DATA.getItemTemplate(itemId).getL10n()));
				}
				if (!containDropItem) {
					lootGroupRules.addItemToBeDistributed(requestedItem);
				}
				return false;
			}
		}
		return true;
	}

	public boolean canAutoLoot(Player player, DropItem requestedItem) {
		int npcId = requestedItem.getNpcObj();
		final DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcId);
		if (dropNpc == null) {
			return false;
		}
		LootGroupRules lootGroupRules = dropNpc.getLootGroupRules();
		if (lootGroupRules == null) {
			return true;
		}

		int itemId = requestedItem.getDropTemplate().getItemId();
		if (itemId == ItemId.KINAH)
			return true;

		int distId = lootGroupRules.getAutodistributionId();
		if (dropNpc.getInRangePlayers().size() <= 1) {
			distId = 0;
			dropNpc.setDistributionId(distId);
		}

		ItemQuality quality = DataManager.ITEM_DATA.getItemTemplate(itemId).getItemQuality();
		if (distId > 1 && lootGroupRules.getQualityRule(quality)) {
			boolean anyOnline = false;
			for (Player member : dropNpc.getInRangePlayers()) {
				Player finalPlayer = World.getInstance().getPlayer(member.getObjectId());
				if (finalPlayer != null && finalPlayer.isOnline()) {
					anyOnline = true;
					break;
				}
			}
			return !anyOnline;
		}
		return true;
	}

	public void requestDropItem(Player player, int npcObjectId, int itemIndex) {
		requestDropItem(player, npcObjectId, itemIndex, false);
	}

	public void requestDropItem(Player player, int npcObjectId, int itemIndex, boolean autoLoot) {
		Set<DropItem> dropItems = DropRegistrationService.getInstance().getCurrentDropMap().get(npcObjectId);
		DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcObjectId);
		// drop was unregistered
		if (dropItems == null || dropNpc == null)
			return;
		// The item's count and winner are shared by all looters of this corpse, whose packets run on their own threads: two of them taking the same item
		// at the same time would both get it, since each takes what it read from the count.
		synchronized (dropNpc) {
			requestDropItem(player, npcObjectId, itemIndex, autoLoot, dropItems, dropNpc);
		}
	}

	private void requestDropItem(Player player, int npcObjectId, int itemIndex, boolean autoLoot, Set<DropItem> dropItems, DropNpc dropNpc) {
		DropItem requestedItem = null;
		synchronized (dropItems) {
			for (DropItem dropItem : dropItems)
				if (dropItem.getIndex() == itemIndex) {
					requestedItem = dropItem;
					break;
				}
		}

		if (requestedItem == null) // lag can cause drops to be displayed long enough for the client to send multiple loot requests when spamming 'C'
			return;

		// fix exploit
		if (!requestedItem.isDistributeItem() && !dropNpc.isAllowedToLoot(player)) {
			return;
		}

		int itemId = requestedItem.getDropTemplate().getItemId();
		ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(itemId);
		if (template.hasLimitOne()) {
			if (player.getInventory().getFirstItemByItemId(itemId) != null
				|| player.getStorage(StorageType.REGULAR_WAREHOUSE.getId()).getFirstItemByItemId(itemId) != null) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CAN_NOT_GET_LORE_ITEM(template.getL10n()));
				return;
			}
		}

		LootGroupRules lootGroupRules = dropNpc.getLootGroupRules();
		if (lootGroupRules != null && !requestedItem.isDistributeItem() && !requestedItem.isFreeForAll()) {
			if (lootGroupRules.containDropItem(requestedItem)) {
				if (!autoLoot)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LOOT_ALREADY_DISTRIBUTING_ITEM(template.getL10n()));
				return;
			}

			if (autoLoot && !canAutoLoot(player, requestedItem))
				return;

			requestedItem.setNpcObj(npcObjectId);
			if (!canDistribute(player, requestedItem)) {
				return;
			}
		}

		long initialCount = requestedItem.getCount();
		// Kinah is distributed to all group/alliance members nearby.
		if (itemId == ItemId.KINAH) {
			var team = player.getCurrentTeam();
			if (team == null) {
				requestedItem.setCount(ItemService.addItem(player, itemId, requestedItem.getCount()));
			} else {
				List<Player> entitledPlayers = team
					.filterMembers(m -> m.isOnline() && !m.isDead() && !m.isMentor() && PositionUtil.isInRange(m, player, GroupConfig.GROUP_MAX_DISTANCE));
				distributeEqually(requestedItem, entitledPlayers);
			}
		} else if (!player.isInTeam() && !requestedItem.isItemWonNotCollected() && dropNpc.getDistributionId() == 0) {
			requestedItem.setCount(ItemService.addItem(player, itemId, requestedItem.getCount()));
		} else if (!requestedItem.isDistributeItem()) {
			if (lootGroupRules != null) {
				ItemQuality quality = DataManager.ITEM_DATA.getItemTemplate(itemId).getItemQuality();
				if (lootGroupRules.isAutoDistributedJunk(quality)) {
					Collection<Player> members = dropNpc.getInRangePlayers();

					if (members.size() > lootGroupRules.getJunkRecipientIndex()) {
						lootGroupRules.setJunkRecipientIndex(lootGroupRules.getJunkRecipientIndex() + 1);
					} else {
						lootGroupRules.setJunkRecipientIndex(1);
					}

					int i = 0;
					for (Player p : members) {
						i++;
						if (i == lootGroupRules.getJunkRecipientIndex()) {
							requestedItem.setWinningPlayer(p);
							break;
						}
					}
				} else {
					requestedItem.setWinningPlayer(player);
				}
			} else if (requestedItem.getWinningPlayer() == null) {
				requestedItem.setWinningPlayer(player);
			}

			if (requestedItem.getWinningPlayer() != null) {
				requestedItem.setCount(ItemService.addItem(requestedItem.getWinningPlayer(), itemId, requestedItem.getCount(), false, new TempTradeDropPredicate(dropNpc)));

				winningNormalActions(player, dropNpc, requestedItem);
			}
		} else if (!autoLoot && requestedItem.isDistributeItem()) { // handles distribution of item to correct player and messages accordingly
			if (!player.equals(requestedItem.getWinningPlayer()) && requestedItem.isItemWonNotCollected()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LOOT_ANOTHER_OWNER_ITEM());
				return;
			} else if (requestedItem.getWinningPlayer().getInventory().isFull(template.getExtraInventoryId())) {
				PacketSendUtility.sendPacket(requestedItem.getWinningPlayer(), SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR());
				requestedItem.isItemWonNotCollected(true);
				return;
			}

			requestedItem.setCount(ItemService.addItem(requestedItem.getWinningPlayer(), itemId, requestedItem.getCount(), false, new TempTradeDropPredicate(dropNpc)));

			if (requestedItem.getDistributionId() == 2)
				winningRollActions(requestedItem.getWinningPlayer(), itemId, npcObjectId);
			// the winner of a bid paid when the bids ended, see payWinningBid
		}

		if (requestedItem.getCount() <= 0) {
			synchronized (dropItems) {
				dropItems.remove(requestedItem);
			}
		}
		if (requestedItem.getCount() < initialCount) {
			announceDrop(requestedItem.getWinningPlayer() != null ? requestedItem.getWinningPlayer() : player, template);
			Pet pet = player.getPet();
			if (pet != null && pet.getCommonData().isSelling()) {
				List<Item> stacks = player.getInventory().getItemsByItemId(requestedItem.getDropTemplate().getItemId());
				if (stacks.stream().anyMatch(item -> item.isSellable() && item.getItemTemplate().getItemQuality() == ItemQuality.JUNK)) {
					PetService.getInstance().sell(pet, stacks);
				}
			}
		}

		if (!autoLoot)
			resendDropList(dropNpc.getLootingPlayer(), npcObjectId, dropNpc, dropItems);
	}

	private static void distributeEqually(DropItem item, List<Player> players) {
		if (players.isEmpty())
			return;
		long countPerPlayer = item.getCount() / players.size();
		for (int i = players.size() - 1; i >= 0; i--) {
			long count = i == 0 ? item.getCount() : countPerPlayer;
			long remainingCount = ItemService.addItem(players.get(i), item.getDropTemplate().getItemId(), count);
			item.setCount(item.getCount() - count + remainingCount);
		}
	}

	private void resendDropList(Player player, int npcObjectId, DropNpc dropNpc, Set<DropItem> dropItems) {
		Npc npc = (Npc) World.getInstance().findVisibleObject(npcObjectId);
		if (dropItems.size() != 0) {
			if (player != null) {
				PacketSendUtility.sendPacket(player, new SM_LOOT_ITEMLIST(dropNpc, dropItems, player));
			}
		} else {
			if (player != null) {
				PacketSendUtility.sendPacket(player, new SM_LOOT_STATUS(npcObjectId, Status.CLOSE_DROP_LIST));
				player.unsetState(CreatureState.LOOTING);
				player.setState(CreatureState.ACTIVE);
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_LOOT, 0, npcObjectId), true);
			}
			if (npc != null) {
				npc.getController().delete();
			}
		}
	}

	private void winningRollActions(Player player, int itemId, int npcObjectId) {
		String itemL10n = DataManager.ITEM_DATA.getItemTemplate(itemId).getL10n();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LOOT_GET_ITEM_ME(itemL10n));

		if (player.isInTeam()) {
			for (Player member : DropRegistrationService.getInstance().getDropRegistrationMap().get(npcObjectId).getInRangePlayers()) {
				if (member != null && !player.equals(member)) {
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_LOOT_GET_ITEM_OTHER(player.getName(), itemL10n));
				}
			}
		}
	}

	/**
	 * Takes the bid from the winner as soon as the bids end and shares it among the other members, so the item is paid for before it can be picked
	 * up. Paying on pickup let a winner who had spent the kinah in the meantime keep the item for free, and charged again for each part of a stack
	 * that didn't fit into the inventory at once.
	 *
	 * @return False if the winner doesn't have the kinah anymore.
	 */
	boolean payWinningBid(Player player, DropItem item, long highestValue) {
		if (player.getInventory().getKinah() < highestValue) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PAY_NOT_ENOUGH_MONEY());
			return false;
		}
		// shared by everyone the item was offered to, also those who passed, not by who stands nearby now
		List<Player> members = item.getOfferedTo().stream().filter(id -> id != player.getObjectId()).map(id -> World.getInstance().getPlayer(id))
			.filter(p -> p != null && p.isOnline()).toList();
		if (members.isEmpty()) // nobody to pay, so the item is free
			return true;
		if (!player.getInventory().tryDecreaseKinah(highestValue)) { // spent since the bid, the next bidder gets the item
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PAY_NOT_ENOUGH_MONEY());
			return false;
		}
		long share = highestValue / members.size();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PAY_ACCOUNT_ME(highestValue));
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_SPLIT_ME_TO_B(highestValue, members.size(), share));
		for (Player member : members) {
			PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_PAY_ACCOUNT_OTHER(player.getName(), highestValue));
			PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_SPLIT_B_TO_ME(player.getName(), highestValue, members.size(), share));
			member.getInventory().increaseKinah(share);
		}
		return true;
	}

	private void winningNormalActions(Player player, DropNpc dropNpc, DropItem requestedItem) {
		if (player == null || dropNpc == null)
			return;

		int itemId = requestedItem.getDropTemplate().getItemId();
		if (player.isInTeam()) {
			for (Player member : dropNpc.getInRangePlayers()) {
				if (member != null && !requestedItem.getWinningPlayer().equals(member) && member.isOnline())
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_GET_ITEM_PARTYNOTICE(requestedItem.getWinningPlayer().getName(),
						DataManager.ITEM_DATA.getItemTemplate(itemId).getL10n()));
			}
		}
	}

	public void see(Player player, Npc npc) {
		if (!npc.isDead())
			return;
		DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npc.getObjectId());
		if (dropNpc != null && dropNpc.isAllowedToLoot(player)) {
			PacketSendUtility.sendPacket(player, new SM_LOOT_STATUS(npc.getObjectId(), Status.LOOT_ENABLE));
		}
	}

	private void announceDrop(Player player, ItemTemplate template) {
		if (DropConfig.MIN_ANNOUNCE_QUALITY == null || player.isInInstance())
			return;
		if (template.getItemQuality().getQualityId() < DropConfig.MIN_ANNOUNCE_QUALITY.getQualityId())
			return;
		PacketSendUtility.broadcastToMap(player, SM_SYSTEM_MESSAGE.STR_FORCE_ITEM_WIN(player.getName(), ChatUtil.item(template.getTemplateId())), 0,
			p -> !p.equals(player) && p.getRace() == player.getRace());
	}

	private static final class TempTradeDropPredicate extends ItemUpdatePredicate {

		private final DropNpc dropNpc;

		private TempTradeDropPredicate(DropNpc dropNpc) {
			this.dropNpc = dropNpc;
		}

		@Override
		public boolean changeItem(Item input) {
			if (dropNpc.getAllowedLooters().size() > 1) {
				ItemTemplate template = input.getItemTemplate();
				if (template.getTempExchangeTime() != 0) {
					input.setTemporaryExchangeTime((int) (System.currentTimeMillis() / 1000) + (template.getTempExchangeTime() * 60));
					TemporaryTradeTimeTask.getInstance().addTask(input, dropNpc.getAllowedLooters());
				}
				return true;
			}
			return false;
		}

	}

	private static class SingletonHolder {

		protected static final DropService instance = new DropService();

	}

}
