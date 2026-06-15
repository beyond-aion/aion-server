package com.aionemu.gameserver.services.drop;

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
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.gameobjects.*;
import com.aionemu.gameserver.model.gameobjects.player.InRoll;
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
		DropRegistrationService.getInstance().getDropRegistrationMap().remove(npcObjId);
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

		if (dropNpc.isBeingLooted()) {
			if (!dropNpc.getLootingPlayer().isOnline()) {
				log.warn(
					dropNpc.getLootingPlayer() + " is offline but was still set as drop looter for " + World.getInstance().findVisibleObject(npcObjectId));
			} else {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_LOOT_FAIL_ONLOOTING());
				return;
			}
		}

		dropNpc.setLootingPlayer(player);
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

		if (!player.equals(dropNpc.getLootingPlayer()))
			return;// cheater :)

		Set<DropItem> dropItems = DropRegistrationService.getInstance().getCurrentDropMap().get(npcObjectId);
		dropNpc.setLootingPlayer(null);

		Npc npc = (Npc) World.getInstance().findVisibleObject(npcObjectId);
		if (npc != null) {
			if (dropItems == null || dropItems.isEmpty()) {
				npc.getController().delete();
				return;
			}

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
			PacketSendUtility.broadcastPacket(npc, new SM_LOOT_STATUS(npcObjectId, Status.LOOT_ENABLE), dropNpc::isAllowedToLoot);
		}
	}

	public boolean canDistribute(Player player, DropItem requestedItem) {
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
					dropNpc.setCurrentIndex(requestedItem.getIndex());
					for (Player member : dropNpc.getInRangePlayers()) {
						Player finalPlayer = World.getInstance().getPlayer(member.getObjectId());
						if (finalPlayer != null && finalPlayer.isOnline()) {
							dropNpc.addPlayerStatus(finalPlayer);
							finalPlayer.setPlayerMode(PlayerMode.IN_ROLL, new InRoll(npcId, itemId, requestedItem.getIndex(), dropNpc.getDistributionId()));
							PacketSendUtility.sendPacket(finalPlayer, new SM_GROUP_LOOT(dropNpc.getLootingTeamId(), 0, itemId, (int) requestedItem.getCount(),
								npcId, dropNpc.getDistributionId(), 1, requestedItem.getIndex()));
						}
					}
					lootGroupRules.setPlayersInRoll(dropNpc.getInRangePlayers(), dropNpc.getDistributionId() == 2 ? 17000 : 32000, requestedItem.getIndex(),
						npcId);
				} else {
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
		DropItem requestedItem = null;
		// drop was unregistered
		if (dropItems == null || dropNpc == null) {
			return;
		}

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
				if (lootGroupRules.isMisc(quality)) {
					Collection<Player> members = dropNpc.getInRangePlayers();

					if (members.size() > lootGroupRules.getNrMisc()) {
						lootGroupRules.setNrMisc(lootGroupRules.getNrMisc() + 1);
					} else {
						lootGroupRules.setNrMisc(1);
					}

					int i = 0;
					for (Player p : members) {
						i++;
						if (i == lootGroupRules.getNrMisc()) {
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

			switch (dropNpc.getDistributionId()) {
				case 2 -> winningRollActions(requestedItem.getWinningPlayer(), itemId, npcObjectId);
				case 3 -> winningBidActions(requestedItem.getWinningPlayer(), npcObjectId, requestedItem.getHighestValue());
			}
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

	private void winningBidActions(Player player, int npcObjectId, long highestValue) {
		DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcObjectId);
		if (highestValue > 0) {
			if (!player.getInventory().tryDecreaseKinah(highestValue)) {
				return;
			}
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PAY_ACCOUNT_ME(highestValue));
		}

		List<Player> onlineMembers = dropNpc.getInRangePlayers().stream().filter(p -> p.isOnline() && !p.equals(player)).toList();
		for (Player member : onlineMembers) {
			PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_PAY_ACCOUNT_OTHER(player.getName(), highestValue));
			long distributeKinah = highestValue / onlineMembers.size();
			member.getInventory().increaseKinah(distributeKinah);
			PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_PAY_DISTRIBUTE(highestValue, onlineMembers.size(), distributeKinah));
		}
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
