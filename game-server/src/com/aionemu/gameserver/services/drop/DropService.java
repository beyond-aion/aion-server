package com.aionemu.gameserver.services.drop;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.objects.filter.ObjectFilter;
import com.aionemu.gameserver.configs.main.DropConfig;
import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.Race;
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
import com.aionemu.gameserver.model.team2.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team2.common.legacy.LootRuleType;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GROUP_LOOT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOT_ITEMLIST;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LOOT_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.services.item.ItemInfoService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.taskmanager.tasks.TemporaryTradeTimeTask;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

import javolution.util.FastTable;

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
						if (npc instanceof Npc && (Race.ASMODIANS == ((Npc) npc).getRace() || Race.ELYOS == ((Npc) npc).getRace())) {
							PacketSendUtility.broadcastPacket(npc, new SM_LOOT_STATUS(npcUniqueId, 0), false, new ObjectFilter<Player>() {

								@Override
								public boolean acceptObject(Player object) {
									return ((Npc) npc).getRace() != object.getRace();
								}
							});
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
		Integer npcObjId = npc.getObjectId();
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
				npc.getController().onDelete();
				return;
			}

			RespawnService.scheduleDecayTask(npc, dropNpc.getRemaingDecayTime());

			LootGroupRules lootGrouRules = player.getLootGroupRules();
			if (lootGrouRules != null && dropNpc.getInRangePlayers().size() > 1 && dropNpc.getPlayersObjectId().size() == 1) {
				LootRuleType lrt = lootGrouRules.getLootRule();
				if (lrt != LootRuleType.FREEFORALL) {
					for (Player member : dropNpc.getInRangePlayers()) {
						if (member != null) {
							Integer object = member.getObjectId();
							dropNpc.setPlayerObjectId(object);
						}
					}
					DropRegistrationService.getInstance().setItemsToWinner(dropItems, 0);
				}
			}
			if (dropNpc.isFreeForAll()) {
				PacketSendUtility.broadcastPacket(npc, new SM_LOOT_STATUS(npcId, 0));
			} else {
				PacketSendUtility.broadcastPacket(player, new SM_LOOT_STATUS(npcId, 0), true, new ObjectFilter<Player>() {

					@Override
					public boolean acceptObject(Player object) {
						return dropNpc.containsKey(object.getObjectId());
					}

				});
			}
		}
	}

	public boolean canDistribute(Player player, DropItem requestedItem) {
		int npcId = requestedItem.getNpcObj();
		final DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcId);
		if (dropNpc == null) {
			return false;
		}
		int itemId = requestedItem.getDropTemplate().getItemId();
		ItemQuality quality = ItemInfoService.getQuality(itemId);
		LootGroupRules lootGrouRules = player.getLootGroupRules();
		if (lootGrouRules == null) {
			return true;
		}

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
						SM_SYSTEM_MESSAGE.STR_MSG_LOOT_ALREADY_DISTRIBUTING_ITEM(new DescriptionId(ItemInfoService.getNameId(itemId))));
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
		ItemQuality quality = ItemInfoService.getQuality(itemId);
		if (itemId == 182400001)
			return true;

		int distId = lootGroupRules.getAutodistributionId();
		if (dropNpc.getGroupSize() <= 1) {
			distId = 0;
			dropNpc.setDistributionId(distId);
		}

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
			log.warn("Null requested index item: " + itemIndex + " npcObjId" + npcObjectId + " player: " + player.getObjectId());
			return;
		}

		// fix exploit
		if (!requestedItem.isDistributeItem() && !dropNpc.containsKey(player.getObjectId()) && !dropNpc.isFreeForAll()) {
			return;
		}

		int itemId = requestedItem.getDropTemplate().getItemId();
		ItemTemplate item = DataManager.ITEM_DATA.getItemTemplate(itemId);
		if (requestedItem.getDropTemplate().getItemTemplate().hasLimitOne()) {
			if (player.getInventory().getFirstItemByItemId(itemId) != null
				|| player.getStorage(StorageType.REGULAR_WAREHOUSE.getId()).getFirstItemByItemId(itemId) != null) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CAN_NOT_GET_LORE_ITEM((new DescriptionId(item.getNameId()))));
				return;
			}
		}

		long currentDropItemCount = requestedItem.getCount();
		ItemQuality quality = ItemInfoService.getQuality(itemId);
		LootGroupRules lootGrouRules = player.getLootGroupRules();
		if (lootGrouRules != null && !requestedItem.isDistributeItem() && !requestedItem.isFreeForAll()) {
			if (lootGrouRules.containDropItem(requestedItem)) {
				if (!autoLoot)
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1390219));
				return;
			}

			if (autoLoot && !canAutoLoot(player, requestedItem))
				return;

			requestedItem.setNpcObj(npcObjectId);
			if (!canDistribute(player, requestedItem)) {
				return;
			}
		}

		// Kinah is distributed to all group/alliance members nearby.
		if (itemId == 182400001) {
			if (player.isInAlliance2() || player.isInGroup2()) {
				Collection<Player> members = player.getPlayerAlliance2() == null ? player.getPlayerGroup2().getMembers()
					: player.getPlayerAlliance2().getMembers();
				List<Player> entitledPlayers = new FastTable<>();
				if (!members.isEmpty() && members.size() > 1) {
					for (Player member : members) {
						if (member != null) {
							if (member.equals(player))
								continue;
							if (member.isOnline() && !member.getLifeStats().isAlreadyDead() && !member.isMentor()
								&& MathUtil.isIn3dRange(member, player, GroupConfig.GROUP_MAX_DISTANCE))
								entitledPlayers.add(member);
						}
					}
					if (!entitledPlayers.isEmpty()) {
						long remainder = currentDropItemCount % (entitledPlayers.size() + 1);// all alliance/group members AND currently looting player
						long kinahForEach = (currentDropItemCount - remainder) / (entitledPlayers.size() + 1); // same here
						currentDropItemCount = 0;

						ItemService.addItem(player, itemId, (kinahForEach + remainder));
						for (Player member : entitledPlayers) {
							if (member != null && member.isOnline())
								ItemService.addItem(member, itemId, kinahForEach);
						}
					}
				}
			}
			if (currentDropItemCount > 0)
				currentDropItemCount = ItemService.addItem(player, itemId, currentDropItemCount);
		} else if (!player.isInGroup2() && !player.isInAlliance2() && !requestedItem.isItemWonNotCollected() && dropNpc.getDistributionId() == 0) {
			currentDropItemCount = ItemService.addItem(player, itemId, currentDropItemCount);
			uniqueDropAnnounce(player, requestedItem);
		}

		if (autoLoot) {
			if (currentDropItemCount <= 0) {
				synchronized (dropItems) {
					dropItems.remove(requestedItem);
				}
			} else
				requestedItem.setCount(currentDropItemCount);
			return;
		} else if (!requestedItem.isDistributeItem()) {
			if (player.isInGroup2() || player.isInAlliance2()) {
				lootGrouRules = player.getLootGroupRules();
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
				currentDropItemCount = ItemService.addItem(requestedItem.getWinningPlayer(), itemId, currentDropItemCount, false,
					new TempTradeDropPredicate(dropNpc));

				winningNormalActions(player, npcObjectId, requestedItem);
				uniqueDropAnnounce(player, requestedItem);
			}
		}

		// handles distribution of item to correct player and messages accordingly
		if (requestedItem.isDistributeItem()) {
			if (player != requestedItem.getWinningPlayer() && requestedItem.isItemWonNotCollected()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LOOT_ANOTHER_OWNER_ITEM());
				return;
			} else if (requestedItem.getWinningPlayer().getInventory().isFull(requestedItem.getDropTemplate().getItemTemplate().getExtraInventoryId())) {
				PacketSendUtility.sendPacket(requestedItem.getWinningPlayer(), SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR());
				requestedItem.isItemWonNotCollected(true);
				return;
			}

			currentDropItemCount = ItemService.addItem(requestedItem.getWinningPlayer(), itemId, currentDropItemCount, false,
				new TempTradeDropPredicate(dropNpc));

			switch (dropNpc.getDistributionId()) {
				case 2:
					winningRollActions(requestedItem.getWinningPlayer(), itemId, npcObjectId);
					break;
				case 3:
					winningBidActions(requestedItem.getWinningPlayer(), npcObjectId, requestedItem.getHighestValue());
			}

			uniqueDropAnnounce(player, requestedItem);
		}

		if (currentDropItemCount <= 0) {
			synchronized (dropItems) {
				dropItems.remove(requestedItem);
			}
		} else
			requestedItem.setCount(currentDropItemCount);

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
				npc.getController().onDelete();
			}
		}
	}

	/**
	 * @param Displays
	 *          messages when item gained via ROLLED
	 */
	private void winningRollActions(Player player, int itemId, int npcObjectId) {
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LOOT_GET_ITEM_ME(new DescriptionId(ItemInfoService.getNameId(itemId))));

		if (player.isInGroup2() || player.isInAlliance2()) {
			for (Player member : DropRegistrationService.getInstance().getDropRegistrationMap().get(npcObjectId).getInRangePlayers()) {
				if (member != null && !player.equals(member) && member.isOnline()) {
					PacketSendUtility.sendPacket(member,
						SM_SYSTEM_MESSAGE.STR_MSG_LOOT_GET_ITEM_OTHER(player.getName(), new DescriptionId(ItemInfoService.getNameId(itemId))));
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

		if (player.isInGroup2() || player.isInAlliance2())
			for (Player member : dropNpc.getInRangePlayers())
				if (member != null && !player.equals(member) && member.isOnline()) {
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_PAY_ACCOUNT_OTHER(player.getName(), highestValue));
					long distributeKinah = highestValue / (dropNpc.getGroupSize() - 1);
					member.getInventory().increaseKinah(distributeKinah);
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_PAY_DISTRIBUTE(highestValue, dropNpc.getGroupSize() - 1, distributeKinah));
				}
	}

	private void winningNormalActions(Player player, int npcId, DropItem requestedItem) {
		DropNpc dropNpc = DropRegistrationService.getInstance().getDropRegistrationMap().get(npcId);
		if (player == null || dropNpc == null)
			return;

		int itemId = requestedItem.getDropTemplate().getItemId();
		if (player.isInGroup2() || player.isInAlliance2()) {
			for (Player member : dropNpc.getInRangePlayers()) {
				if (member != null && !requestedItem.getWinningPlayer().equals(member) && member.isOnline())
					PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_GET_ITEM_PARTYNOTICE(requestedItem.getWinningPlayer().getName(),
						new DescriptionId(ItemInfoService.getNameId(itemId))));
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

	private void uniqueDropAnnounce(final Player player, final DropItem requestedItem) {
		if (DropConfig.ENABLE_UNIQUE_DROP_ANNOUNCE
			&& !player.getInventory().isFull(requestedItem.getDropTemplate().getItemTemplate().getExtraInventoryId())) {
			final ItemTemplate itemTemplate = ItemInfoService.getItemTemplate(requestedItem.getDropTemplate().getItemId());

			if (itemTemplate.getItemQuality() == ItemQuality.UNIQUE || itemTemplate.getItemQuality() == ItemQuality.EPIC) {
				final String lastGetName = requestedItem.getWinningPlayer() != null ? requestedItem.getWinningPlayer().getName() : player.getName();
				final int pObjectId = player.getObjectId();
				final int pRaceId = player.getRace().getRaceId();
				final int pMapId = player.getWorldId();
				final int pInstance = player.isInInstance() ? player.getInstanceId() : 0;

				World.getInstance().doOnAllPlayers(new Visitor<Player>() {

					@Override
					public void visit(Player other) {

						int oObjectId = other.getObjectId();
						int oRaceId = other.getRace().getRaceId();
						int oMapId = other.getWorldId();
						int oInstance = other.isInInstance() ? other.getInstanceId() : 0;

						if (oObjectId != pObjectId && other.isSpawned() && oRaceId == pRaceId && oMapId == pMapId && oInstance == pInstance) {
							PacketSendUtility.sendPacket(other,
								new SM_SYSTEM_MESSAGE(1390003, lastGetName, "[item: " + requestedItem.getDropTemplate().getItemId() + "]"));
						}
					}

				});
			}
		}
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
