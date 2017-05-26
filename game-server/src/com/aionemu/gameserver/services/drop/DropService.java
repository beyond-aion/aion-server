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
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.drop.DropItem;
import com.aionemu.gameserver.model.gameobjects.DropNpc;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.InRoll;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team.common.legacy.LootRuleType;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_LOOT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOT_ITEMLIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOT_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.taskmanager.tasks.TemporaryTradeTimeTask;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer, xTz
 */
public class DropService {

	private static final Logger log = LoggerFactory.getLogger(DropService.class);

	public static DropService getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * @param npcUniqueId
	 */
	public void scheduleFreeForAll(final int npcUniqueId) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcUniqueId);
				if (dropNpc != null) {
					DropRegistrationService.getInstance().getDropRegistrationMap().get(npcUniqueId).startFreeForAll();
					VisibleObject npc = World.getInstance().findVisibleObject(npcUniqueId);
					if (npc != null && npc.isSpawned()) {
						// fix for elyos/asmodians being able to loot elyos/asmodian npcs
						// TODO there might be more npcs who are friendly towards players and should not be loot able by them
						if (npc instanceof Npc && ((Npc) npc).getRace().isAsmoOrEly()) {
							PacketSendUtility.broadcastPacket(npc, new SM_LOOT_STATUS(npcUniqueId, 0), p -> ((Npc) npc).getRace() != p.getRace());
						} else {
							PacketSendUtility.broadcastPacket(npc, new SM_LOOT_STATUS(npcUniqueId, 0));
						}
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
	 * @param npcId
	 */
	public void requestDropList(Player player, int npcId) {
		DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcId);
		if (player == null || dropNpc == null) {
			return;
		}

		if (!dropNpc.containsKey(player.getObjectId()) && !dropNpc.isFreeForAll()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_LOOT_NO_RIGHT());
			return;
		}

		if (dropNpc.isBeingLooted()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_LOOT_FAIL_ONLOOTING());
			return;
		}

		dropNpc.setBeingLooted(player);
		VisibleObject visObj = World.getInstance().findVisibleObject(npcId);
		if (visObj instanceof Npc) {
			Npc npc = ((Npc) visObj);
			ScheduledFuture<?> decayTask = (ScheduledFuture<?>) npc.getController().cancelTask(TaskId.DECAY);
			if (decayTask != null) {
				long remaingDecayTime = decayTask.getDelay(TimeUnit.MILLISECONDS);
				dropNpc.setRemaingDecayTime(remaingDecayTime);
			}
		}

		Set<DropItem> dropItems = DropRegistrationService.getInstance().getCurrentDropMap().get(npcId);

		if (dropItems == null) {
			dropItems = Collections.emptySet();
		}

		PacketSendUtility.sendPacket(player, new SM_LOOT_ITEMLIST(npcId, dropItems, player));
		PacketSendUtility.sendPacket(player, new SM_LOOT_STATUS(npcId, 2));
		player.unsetState(CreatureState.ACTIVE);
		player.setState(CreatureState.LOOTING);
		player.setLootingNpcOid(npcId);
		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_LOOT, 0, npcId), true);
	}

	/**
	 * This method will change looted corpse to not in use
	 *
	 * @param player
	 * @param npcId
	 */
	public void closeDropList(Player player, int npcId) {
		final DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcId);
		if (dropNpc == null)
			return;

		player.unsetState(CreatureState.LOOTING);
		player.setState(CreatureState.ACTIVE);
		player.setLootingNpcOid(0);

		PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_LOOT, 0, npcId), true);

		if (dropNpc.getBeingLooted() != player)
			return;// cheater :)

		Set<DropItem> dropItems = DropRegistrationService.getInstance().getCurrentDropMap().get(npcId);
		dropNpc.setBeingLooted(null);

		Npc npc = (Npc) World.getInstance().findVisibleObject(npcId);
		if (npc != null) {
			if (dropItems == null || dropItems.isEmpty()) {
				npc.getController().delete();
				return;
			}

			RespawnService.scheduleDecayTask(npc, dropNpc.getRemaingDecayTime());

			LootGroupRules lootGrouRules = player.getLootGroupRules();
			if (lootGrouRules != null && dropNpc.getInRangePlayers().size() > 1 && dropNpc.getPlayersObjectId().size() == 1) {
				LootRuleType lrt = lootGrouRules.getLootRule();
				if (lrt != LootRuleType.FREEFORALL) {
					for (Player member : dropNpc.getInRangePlayers()) {
						if (member != null)
							dropNpc.setPlayerObjectId(member.getObjectId());
					}
					DropRegistrationService.getInstance().setItemsToWinner(dropItems, 0);
				}
			}
			if (dropNpc.isFreeForAll()) {
				PacketSendUtility.broadcastPacket(npc, new SM_LOOT_STATUS(npcId, 0));
			} else {
				PacketSendUtility.broadcastPacket(player, new SM_LOOT_STATUS(npcId, 0), true, p -> dropNpc.containsKey(p.getObjectId()));
			}
		}
	}

	public boolean canDistribute(Player player, DropItem requestedItem) {
		int npcId = requestedItem.getNpcObj();
		final DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcId);
		if (dropNpc == null) {
			return false;
		}
		LootGroupRules lootGrouRules = player.getLootGroupRules();
		if (lootGrouRules == null) {
			return true;
		}

		int itemId = requestedItem.getDropTemplate().getItemId();
		ItemQuality quality = DataManager.ITEM_DATA.getItemTemplate(itemId).getItemQuality();
		if (itemId != 182400001) {
			lootGrouRules = player.getLootGroupRules();
			if (dropNpc.getGroupSize() > 1) {
				dropNpc.setDistributionId(lootGrouRules.getAutodistributionId());
				dropNpc.setDistributionType(lootGrouRules.getQualityRule(quality));
			} else
				dropNpc.setDistributionId(0);
			if (dropNpc.getDistributionId() > 1 && dropNpc.getDistributionType()) {
				boolean containDropItem = lootGrouRules.containDropItem(requestedItem);
				if (lootGrouRules.getItemsToBeDistributed().isEmpty() || containDropItem) {
					dropNpc.setCurrentIndex(requestedItem.getIndex());
					for (Player member : dropNpc.getInRangePlayers()) {
						Player finalPlayer = World.getInstance().findPlayer(member.getObjectId());
						if (finalPlayer != null && finalPlayer.isOnline()) {
							dropNpc.addPlayerStatus(finalPlayer);
							finalPlayer.setPlayerMode(PlayerMode.IN_ROLL, new InRoll(npcId, itemId, requestedItem.getIndex(), dropNpc.getDistributionId()));
							PacketSendUtility.sendPacket(finalPlayer,
								new SM_GROUP_LOOT(finalPlayer.getCurrentTeamId(), 0, itemId, npcId, dropNpc.getDistributionId(), 1, requestedItem.getIndex()));
						}
					}
					lootGrouRules.setPlayersInRoll(dropNpc.getInRangePlayers(), dropNpc.getDistributionId() == 2 ? 17000 : 32000, requestedItem.getIndex(),
						npcId);
					if (!containDropItem) {
						lootGrouRules.addItemToBeDistributed(requestedItem);
					}
					return false;
				} else {
					PacketSendUtility.sendPacket(player,
						SM_SYSTEM_MESSAGE.STR_MSG_LOOT_ALREADY_DISTRIBUTING_ITEM(DataManager.ITEM_DATA.getItemTemplate(itemId).getNameId()));
					if (!containDropItem) {
						lootGrouRules.addItemToBeDistributed(requestedItem);
					}
					return false;
				}
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
		LootGroupRules lootGroupRules = player.getLootGroupRules();
		if (lootGroupRules == null) {
			return true;
		}

		int itemId = requestedItem.getDropTemplate().getItemId();
		if (itemId == 182400001)
			return true;

		int distId = lootGroupRules.getAutodistributionId();
		if (dropNpc.getGroupSize() <= 1) {
			distId = 0;
			dropNpc.setDistributionId(distId);
		}

		ItemQuality quality = DataManager.ITEM_DATA.getItemTemplate(itemId).getItemQuality();
		if (distId > 1 && lootGroupRules.getQualityRule(quality)) {
			boolean anyOnline = false;
			for (Player member : dropNpc.getInRangePlayers()) {
				Player finalPlayer = World.getInstance().findPlayer(member.getObjectId());
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

		if (requestedItem == null) {
			log.warn(player + " requested drop at invalid index " + itemIndex + " from " + World.getInstance().findVisibleObject(npcObjectId));
			return;
		}

		// fix exploit
		if (!requestedItem.isDistributeItem() && !dropNpc.containsKey(player.getObjectId()) && !dropNpc.isFreeForAll()) {
			return;
		}

		int itemId = requestedItem.getDropTemplate().getItemId();
		ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(itemId);
		if (template.hasLimitOne()) {
			if (player.getInventory().getFirstItemByItemId(itemId) != null
				|| player.getStorage(StorageType.REGULAR_WAREHOUSE.getId()).getFirstItemByItemId(itemId) != null) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CAN_NOT_GET_LORE_ITEM(new DescriptionId(template.getNameId())));
				return;
			}
		}

		LootGroupRules lootGrouRules = player.getLootGroupRules();
		if (lootGrouRules != null && !requestedItem.isDistributeItem() && !requestedItem.isFreeForAll()) {
			if (lootGrouRules.containDropItem(requestedItem)) {
				if (!autoLoot)
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LOOT_ALREADY_DISTRIBUTING_ITEM(template.getNameId()));
				return;
			}

			if (autoLoot && !canAutoLoot(player, requestedItem))
				return;

			requestedItem.setNpcObj(npcObjectId);
			if (!canDistribute(player, requestedItem)) {
				return;
			}
		}

		long remainingCount = requestedItem.getCount();
		// Kinah is distributed to all group/alliance members nearby.
		if (itemId == 182400001) {
			if (player.isInTeam()) {
				List<Player> entitledPlayers = new ArrayList<>();
				for (Player member : player.getCurrentTeam().getMembers()) {
					if (member.isOnline() && !member.getLifeStats().isAlreadyDead() && !member.isMentor()
						&& PositionUtil.isInRange(member, player, GroupConfig.GROUP_MAX_DISTANCE))
						entitledPlayers.add(member);
				}
				if (entitledPlayers.isEmpty()) {
					VisibleObject npc = World.getInstance().findVisibleObject(npcObjectId);
					AuditLogger.log(player, "tried to loot kinah for team from " + npc + " but he is not allowed to (mentor=" + player.isMentor() + ", dead="
						+ player.getLifeStats().isAlreadyDead() + ")");
					return;
				}
				long remainder = remainingCount % entitledPlayers.size();
				long kinahForEach = (remainingCount - remainder) / entitledPlayers.size();
				remainingCount = 0;
				for (Player member : entitledPlayers)
					remainingCount += ItemService.addItem(member, itemId, member.equals(player) ? kinahForEach + remainder : kinahForEach);
			} else {
				remainingCount = ItemService.addItem(player, itemId, remainingCount);
			}
		} else if (!player.isInTeam() && !requestedItem.isItemWonNotCollected() && dropNpc.getDistributionId() == 0) {
			remainingCount = ItemService.addItem(player, itemId, remainingCount);
		}

		if (autoLoot) {
			if (remainingCount <= 0) {
				synchronized (dropItems) {
					dropItems.remove(requestedItem);
				}
				announceDrop(player, template);
			} else
				requestedItem.setCount(remainingCount);
			return;
		} else if (!requestedItem.isDistributeItem()) {
			if (player.isInTeam()) {
				lootGrouRules = player.getLootGroupRules();
				ItemQuality quality = DataManager.ITEM_DATA.getItemTemplate(itemId).getItemQuality();
				if (lootGrouRules.isMisc(quality)) {
					Collection<Player> members = dropNpc.getInRangePlayers();

					if (members.size() > lootGrouRules.getNrMisc()) {
						lootGrouRules.setNrMisc(lootGrouRules.getNrMisc() + 1);
					} else {
						lootGrouRules.setNrMisc(1);
					}

					int i = 0;
					for (Player p : members) {
						i++;
						if (i == lootGrouRules.getNrMisc()) {
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
				remainingCount = ItemService.addItem(requestedItem.getWinningPlayer(), itemId, remainingCount, false, new TempTradeDropPredicate(dropNpc));

				winningNormalActions(player, dropNpc, requestedItem);
			}
		} else if (requestedItem.isDistributeItem()) { // handles distribution of item to correct player and messages accordingly
			if (!player.equals(requestedItem.getWinningPlayer()) && requestedItem.isItemWonNotCollected()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LOOT_ANOTHER_OWNER_ITEM());
				return;
			} else if (requestedItem.getWinningPlayer().getInventory().isFull(template.getExtraInventoryId())) {
				PacketSendUtility.sendPacket(requestedItem.getWinningPlayer(), SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR());
				requestedItem.isItemWonNotCollected(true);
				return;
			}

			remainingCount = ItemService.addItem(requestedItem.getWinningPlayer(), itemId, remainingCount, false, new TempTradeDropPredicate(dropNpc));

			switch (dropNpc.getDistributionId()) {
				case 2:
					winningRollActions(requestedItem.getWinningPlayer(), itemId, npcObjectId);
					break;
				case 3:
					winningBidActions(requestedItem.getWinningPlayer(), npcObjectId, requestedItem.getHighestValue());
			}
		}

		if (remainingCount <= 0) {
			synchronized (dropItems) {
				dropItems.remove(requestedItem);
			}
			announceDrop(requestedItem.getWinningPlayer() != null ? requestedItem.getWinningPlayer() : player, template);
		} else
			requestedItem.setCount(remainingCount);

		resendDropList(dropNpc.getBeingLooted(), npcObjectId, dropItems);
	}

	private void resendDropList(Player player, int npcObjectId, Set<DropItem> dropItems) {
		Npc npc = (Npc) World.getInstance().findVisibleObject(npcObjectId);
		if (dropItems.size() != 0) {
			if (player != null) {
				PacketSendUtility.sendPacket(player, new SM_LOOT_ITEMLIST(npcObjectId, dropItems, player));
			}
		} else {
			if (player != null) {
				PacketSendUtility.sendPacket(player, new SM_LOOT_STATUS(npcObjectId, 3));
				player.unsetState(CreatureState.LOOTING);
				player.setState(CreatureState.ACTIVE);
				PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_LOOT, 0, npcObjectId), true);
			}
			if (npc != null) {
				npc.getController().delete();
			}
		}
	}

	/**
	 * @param Displays
	 *          messages when item gained via ROLLED
	 */
	private void winningRollActions(Player player, int itemId, int npcObjectId) {
		int nameId = DataManager.ITEM_DATA.getItemTemplate(itemId).getNameId();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LOOT_GET_ITEM_ME(nameId));

		if (player.isInTeam()) {
			for (Player member : DropRegistrationService.getInstance().getDropRegistrationMap().get(npcObjectId).getInRangePlayers()) {
				if (member != null && !player.equals(member)) {
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_LOOT_GET_ITEM_OTHER(player.getName(), nameId));
				}
			}
		}
	}

	/**
	 * @param Displays
	 *          messages/removes and shares kinah when item gained via BID
	 */
	private void winningBidActions(Player player, int npcObjectId, long highestValue) {
		DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcObjectId);
		if (highestValue > 0) {
			if (!player.getInventory().tryDecreaseKinah(highestValue)) {
				return;
			}
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_PAY_ACCOUNT_ME(highestValue));
		}

		if (player.isInGroup() || player.isInAlliance())
			for (Player member : dropNpc.getInRangePlayers())
				if (member != null && !player.equals(member) && member.isOnline()) {
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_PAY_ACCOUNT_OTHER(player.getName(), highestValue));
					long distributeKinah = highestValue / (dropNpc.getGroupSize() - 1);
					member.getInventory().increaseKinah(distributeKinah);
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_PAY_DISTRIBUTE(highestValue, dropNpc.getGroupSize() - 1, distributeKinah));
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
						DataManager.ITEM_DATA.getItemTemplate(itemId).getNameId()));
			}
		}
	}

	public void see(final Player player, Npc owner) {
		final int id = owner.getObjectId();
		final DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(id);

		if (dropNpc == null)
			return;

		if (dropNpc.containsKey(player.getObjectId()) || dropNpc.isFreeForAll()) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					PacketSendUtility.sendPacket(player, new SM_LOOT_STATUS(id, 0));
				}

			}, 5000);
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
			if (dropNpc.getPlayersObjectId().size() > 1) {
				ItemTemplate template = input.getItemTemplate();
				if (template.getTempExchangeTime() != 0) {
					input.setTemporaryExchangeTime((int) (System.currentTimeMillis() / 1000) + (template.getTempExchangeTime() * 60));
					TemporaryTradeTimeTask.getInstance().addTask(input, dropNpc.getPlayersObjectId());
				}
				return true;
			}
			return false;
		}

	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final DropService instance = new DropService();

	}

}
