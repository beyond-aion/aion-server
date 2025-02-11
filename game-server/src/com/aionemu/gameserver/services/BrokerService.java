package com.aionemu.gameserver.services;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.dao.BrokerDAO;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.broker.BrokerItemMask;
import com.aionemu.gameserver.model.broker.BrokerMessages;
import com.aionemu.gameserver.model.broker.BrokerPlayerCache;
import com.aionemu.gameserver.model.broker.BrokerRace;
import com.aionemu.gameserver.model.gameobjects.BrokerItem;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_BROKER_SERVICE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_ITEM;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.restrictions.PlayerRestrictions;
import com.aionemu.gameserver.services.item.ItemFactory;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.services.trade.PricesService;
import com.aionemu.gameserver.taskmanager.AbstractFIFOPeriodicTaskManager;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.collections.DynamicServerPacketBodySplitList;
import com.aionemu.gameserver.utils.collections.ListPart;
import com.aionemu.gameserver.utils.collections.SplitList;
import com.aionemu.gameserver.world.World;

/**
 * @author kosyachok, ATracer, Sykra
 */
public class BrokerService {

	private Map<Integer, BrokerItem> elyosBrokerItems = new ConcurrentHashMap<>();
	private Map<Integer, BrokerItem> elyosSettledItems = new ConcurrentHashMap<>();
	private Map<Integer, BrokerItem> asmodianBrokerItems = new ConcurrentHashMap<>();
	private Map<Integer, BrokerItem> asmodianSettledItems = new ConcurrentHashMap<>();
	private static final Logger log = LoggerFactory.getLogger("EXCHANGE_LOG");
	private static final int DELAY_BROKER_SAVE = 6000;
	private static final int DELAY_BROKER_CHECK = 60000;
	private BrokerPeriodicTaskManager saveManager;
	private Map<Integer, BrokerPlayerCache> playerBrokerCache = new ConcurrentHashMap<>();

	public static BrokerService getInstance() {
		return SingletonHolder.instance;
	}

	private BrokerService() {
		initBrokerService();

		saveManager = new BrokerPeriodicTaskManager(DELAY_BROKER_SAVE);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this::checkExpiredItems, DELAY_BROKER_CHECK, DELAY_BROKER_CHECK);
	}

	private void initBrokerService() {
		log.info("Loading broker...");
		int loadedBrokerItemsCount = 0;
		int loadedSettledItemsCount = 0;

		List<BrokerItem> brokerItems = BrokerDAO.loadBroker();

		for (BrokerItem item : brokerItems) {
			if (item.getItemBrokerRace() == BrokerRace.ASMODIAN) {
				if (item.isSettled()) {
					asmodianSettledItems.put(item.getItemUniqueId(), item);
					loadedSettledItemsCount++;
				} else {
					asmodianBrokerItems.put(item.getItemUniqueId(), item);
					loadedBrokerItemsCount++;
				}
			} else if (item.getItemBrokerRace() == BrokerRace.ELYOS) {
				if (item.isSettled()) {
					elyosSettledItems.put(item.getItemUniqueId(), item);
					loadedSettledItemsCount++;
				} else {
					elyosBrokerItems.put(item.getItemUniqueId(), item);
					loadedBrokerItemsCount++;
				}
			}
		}

		log.info("Broker loaded with " + loadedBrokerItemsCount + " broker items, " + loadedSettledItemsCount + " settled items.");
	}

	public void showRequestedItems(Player player, int clientMask, byte sortType, int startPage, List<Integer> itemList) {
		BrokerItem[] searchItems = null;
		int playerBrokerMaskCache = getPlayerMask(player);
		BrokerItemMask brokerMaskById = BrokerItemMask.getBrokerMaskById(clientMask);
		boolean isChidrenMask = brokerMaskById.isChildrenMask(playerBrokerMaskCache);
		if (itemList != null && clientMask == 0) {
			Map<Integer, BrokerItem> brokerItems = getRaceBrokerItems(player.getRace());
			if (brokerItems == null)
				return;
			searchItems = brokerItems.values().toArray(new BrokerItem[brokerItems.values().size()]);
		} else if ((getFilteredItems(player).length == 0 || !isChidrenMask) && clientMask != 0) {
			searchItems = getItemsByMask(player, clientMask, false);
		} else if (isChidrenMask) {
			searchItems = getItemsByMask(player, clientMask, true);
		} else
			searchItems = getFilteredItems(player);

		if (searchItems == null)
			return;

		getPlayerCache(player).setBrokerSortTypeCache(sortType);
		getPlayerCache(player).setBrokerStartPageCache(startPage);

		if (itemList != null) {
			List<BrokerItem> itemsFound = new ArrayList<>();
			for (BrokerItem item : searchItems) {
				if (itemList.contains(item.getItemId()))
					itemsFound.add(item);
			}
			getPlayerCache(player).setSearchItemsList(itemList);
			searchItems = itemsFound.toArray(new BrokerItem[itemsFound.size()]);
			getPlayerCache(player).setBrokerListCache(searchItems);
		} else
			getPlayerCache(player).setSearchItemsList(null);

		sortBrokerItems(searchItems, sortType);
		int totalSearchItemsCount = searchItems.length;
		searchItems = getRequestedPage(searchItems, startPage);

		for (BrokerItem bi : searchItems) {
			if (bi.getAveragePrice() == 0) {
				bi.setAveragePrice(getAveragePrice(player.getRace(), bi.getItemId()));
			}
		}

		PacketSendUtility.sendPacket(player, new SM_BROKER_SERVICE(searchItems, totalSearchItemsCount, startPage));
	}

	public long getAveragePrice(Race race, int itemId) {
		BrokerItem[] searchItems = null;

		Map<Integer, BrokerItem> brokerItems = getRaceBrokerItems(race);
		if (brokerItems == null)
			return 0;

		long average = 0, sum = 0;
		int counter = 0;

		searchItems = brokerItems.values().toArray(new BrokerItem[brokerItems.values().size()]);

		for (BrokerItem item : searchItems) {
			if (itemId == item.getItemId()) {
				sum += item.getPrice();
				counter++;
			}
		}
		average = sum / counter;
		return average;
	}

	private BrokerItem[] getItemsByMask(Player player, int clientMask, boolean cached) {
		List<BrokerItem> searchItems = new ArrayList<>();

		BrokerItemMask brokerMask = BrokerItemMask.getBrokerMaskById(clientMask);

		if (cached) {
			BrokerItem[] brokerItems = getFilteredItems(player);
			if (brokerItems == null)
				return null;

			for (BrokerItem item : brokerItems) {
				if (item == null || item.getItem() == null)
					continue;

				if (brokerMask.isMatches(item.getItem())) {
					searchItems.add(item);
				}
			}
		} else {
			Map<Integer, BrokerItem> brokerItems = getRaceBrokerItems(player.getRace());
			if (brokerItems == null)
				return null;
			for (BrokerItem item : brokerItems.values()) {
				if (item == null || item.getItem() == null)
					continue;

				if (brokerMask.isMatches(item.getItem())) {
					searchItems.add(item);
				}
			}
		}

		BrokerItem[] items = searchItems.toArray(new BrokerItem[searchItems.size()]);
		getPlayerCache(player).setBrokerListCache(items);
		getPlayerCache(player).setBrokerMaskCache(clientMask);

		return items;
	}

	private void sortBrokerItems(BrokerItem[] brokerItems, byte sortType) {
		Arrays.sort(brokerItems, BrokerItem.getComparatoryByType(sortType));
	}

	private BrokerItem[] getRequestedPage(BrokerItem[] brokerItems, int startPage) {
		List<BrokerItem> page = new ArrayList<>();
		int startingElement = startPage * 9;

		for (int i = startingElement, limit = 0; i < brokerItems.length && limit < 45; i++, limit++) {
			page.add(brokerItems[i]);
		}

		return page.toArray(new BrokerItem[page.size()]);
	}

	private Map<Integer, BrokerItem> getRaceBrokerItems(Race race) {
		switch (race) {
			case ELYOS:
				return elyosBrokerItems;
			case ASMODIANS:
				return asmodianBrokerItems;
			default:
				return null;
		}
	}

	private Map<Integer, BrokerItem> getRaceBrokerSettledItems(Race race) {
		switch (race) {
			case ELYOS:
				return elyosSettledItems;
			case ASMODIANS:
				return asmodianSettledItems;
			default:
				return null;
		}
	}

	public void buyBrokerItem(Player player, int itemUniqueId, long itemCount) {

		boolean isEmptyCache = getFilteredItems(player).length == 0;
		Race playerRace = player.getRace();

		if (!PlayerRestrictions.canTrade(player))
			return;

		synchronized (this) {
			BrokerItem buyingItem = getRaceBrokerItems(playerRace).get(itemUniqueId);
			if (buyingItem == null)
				return; // TODO: Message "this item has already been bought, refresh page please."

			if (buyingItem.getSellerId() == player.getObjectId()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_VENDOR_CAN_NOT_BUY_MY_REGISTER_ITEM());
				return;
			}

			if (buyingItem.isSold() || buyingItem.isCanceled()) {
				LoggerFactory.getLogger(BrokerService.class).warn(
					"Player {} tried to buy the following item[id={}, objId={}, sellerId={}, sellerName={}, sold={}, canceled={}, settled={}, expireTime={}] which is already sold or canceled",
					player.getName(), buyingItem.getItemId(), buyingItem.getItemUniqueId(), buyingItem.getSellerId(),
					PlayerService.getPlayerName(buyingItem.getSellerId()), buyingItem.isSold(), buyingItem.isCanceled(), buyingItem.isSettled(),
					buyingItem.getExpireTime());
				PacketSendUtility.sendMessage(player, "Sorry, but this item already sold");
				return;
			}

			Item item = buyingItem.getItem();
			long price = buyingItem.getPrice() * itemCount;
			if (player.getInventory().isFull(item.getItemTemplate().getExtraInventoryId())) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_FULL_INVENTORY());
				return;
			}

			if (player.getInventory().getKinah() < price)
				return;

			if (buyingItem.getItemCount() > itemCount && buyingItem.isSplittingAvailable()) {
				buyingItem.decreaseItemCount(itemCount);
				buyingItem.setPersistentState(PersistentState.UPDATE_REQUIRED);
				// storing old broker item with rest items to sell
				BrokerOpSaveTask bost = new BrokerOpSaveTask(buyingItem, buyingItem.getItem(), player.getInventory().getKinahItem(), player.getObjectId());
				saveManager.add(bost);
				// creating new broker item which will be settled
				BrokerItem soldItem = new BrokerItem(ItemFactory.newItem(buyingItem.getItemId(), itemCount), buyingItem.getPrice(), buyingItem.getSellerId(),
					buyingItem.isSplittingAvailable(), buyingItem.getItemBrokerRace());
				buyingItem = soldItem;
				item = buyingItem.getItem();
				BrokerOpSaveTask bost2 = new BrokerOpSaveTask(buyingItem, buyingItem.getItem(), player.getInventory().getKinahItem(), player.getObjectId());
				saveManager.add(bost2);
			} else {
				getRaceBrokerItems(playerRace).remove(itemUniqueId);
			}

			putToSettled(playerRace, buyingItem, true);

			if (!isEmptyCache) {
				BrokerItem[] newCache = ArrayUtils.removeElement(getFilteredItems(player), buyingItem);
				getPlayerCache(player).setBrokerListCache(newCache);
			}

			player.getInventory().decreaseKinah(price);
			// unpack
			if (item.getPackCount() > 0) {
				item.setPackCount(item.getPackCount() * -1);
			}
			Item boughtItem = player.getInventory().add(item, ItemPacketService.ItemAddType.BROKER_BUY);

			if (LoggingConfig.LOG_BROKER_EXCHANGE)
				log.info("Player: " + player.getName() + " bought item " + boughtItem.getItemId() + " [" + boughtItem.getItemName() + "] (count: " + itemCount
					+ ") from player: " + PlayerService.getPlayerName(buyingItem.getSellerId()) + " (total price: " + price + ")");

			// create save task
			BrokerOpSaveTask bost = new BrokerOpSaveTask(buyingItem, boughtItem, player.getInventory().getKinahItem(), player.getObjectId());
			saveManager.add(bost);
		}
		showRequestedItems(player, getPlayerCache(player).getBrokerMaskCache(), getPlayerCache(player).getBrokerSortTypeCache(),
			getPlayerCache(player).getBrokerStartPageCache(), getPlayerCache(player).getSearchItemList());

	}

	private void putToSettled(Race race, BrokerItem brokerItem, boolean isSold) {
		if (isSold)
			brokerItem.removeItem();
		else
			brokerItem.setSettled();

		brokerItem.setPersistentState(PersistentState.UPDATE_REQUIRED);

		switch (race) {
			case ASMODIANS:
				asmodianSettledItems.put(brokerItem.getItemUniqueId(), brokerItem);
				break;

			case ELYOS:
				elyosSettledItems.put(brokerItem.getItemUniqueId(), brokerItem);
				break;
		}
		saveManager.add(new BrokerOpSaveTask(brokerItem));
		Player seller = World.getInstance().getPlayer(brokerItem.getSellerId());
		if (seller != null) {
			PacketSendUtility.sendPacket(seller, new SM_BROKER_SERVICE(true, getEarnedKinahFromSoldItems(seller.getRace(), seller.getObjectId())));
			// TODO: Retail system message
		}
	}

	private int getRegisteredItemsCount(Player player) {
		int playerId = player.getObjectId();
		int c = 0;
		for (BrokerItem item : getRaceBrokerItems(player.getRace()).values()) {
			if (item != null && playerId == item.getSellerId())
				c++;
		}
		return c;
	}

	public void registerItem(Player player, int itemUniqueId, long count, long price, boolean splittingAvailable) {
		Item itemToRegister = player.getInventory().getItemByObjId(itemUniqueId);
		Race playerRace = player.getRace();

		if (itemToRegister == null || count > itemToRegister.getItemCount())
			return;

		if (!PlayerRestrictions.canTrade(player)) {
			return;
		}

		if (price <= 0 || count <= 0)
			return;

		if (count > 1 && price / count > 999_999_999 || price > 99_999_999_999L ) { // retail price limits
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_LIMITED_VENDOR_CANT_OVER_GOLD());
			return;
		}

		// Check Trade Hack
		if (itemToRegister.getPackCount() <= 0 && !itemToRegister.isTradeable())
			return;

		if (!AdminService.getInstance().canOperate(player, null, itemToRegister, "broker"))
			return;

		BrokerRace brRace;

		if (playerRace == Race.ASMODIANS)
			brRace = BrokerRace.ASMODIAN;
		else if (playerRace == Race.ELYOS)
			brRace = BrokerRace.ELYOS;
		else
			return;

		int registeredItemsCount = getRegisteredItemsCount(player);
		long registrationCommition = 0;
		if (registeredItemsCount > 14) {
			PacketSendUtility.sendPacket(player, new SM_BROKER_SERVICE(BrokerMessages.NO_SPACE_AVAIABLE.getId()));
			return;
		} else if (registeredItemsCount > 9) // round down in order to match client prices
			registrationCommition = (long) (price * count * 0.04f);
		else
			registrationCommition = (long) (price * count * 0.02f);

		if (registrationCommition < 10)
			registrationCommition = 10;
		else
			registrationCommition = PricesService.getPriceForService(registrationCommition, player.getRace());

		if (player.getInventory().getKinah() < registrationCommition) {
			PacketSendUtility.sendPacket(player, new SM_BROKER_SERVICE(BrokerMessages.NO_ENOUGHT_KINAH.getId()));
			return;
		}
		if (!itemToRegister.getItemTemplate().isStackable())
			splittingAvailable = false;

		player.getInventory().decreaseKinah(registrationCommition);
		if (itemToRegister.getItemTemplate().isStackable() && count < itemToRegister.getItemCount()) {
			int itemId = itemToRegister.getItemId();
			player.getInventory().decreaseItemCount(itemToRegister, count);
			itemToRegister = ItemFactory.newItem(itemId, count);
		} else {
			player.getInventory().remove(itemToRegister);
			PacketSendUtility.sendPacket(player, new SM_DELETE_ITEM(itemToRegister.getObjectId()));
		}

		itemToRegister.setItemLocation(StorageType.BROKER.getId());

		BrokerItem newBrokerItem = new BrokerItem(itemToRegister, price, player.getObjectId(), splittingAvailable, brRace);

		switch (brRace) {
			case ASMODIAN:
				asmodianBrokerItems.put(newBrokerItem.getItemUniqueId(), newBrokerItem);
				break;

			case ELYOS:
				elyosBrokerItems.put(newBrokerItem.getItemUniqueId(), newBrokerItem);
				break;
		}

		BrokerOpSaveTask bost = new BrokerOpSaveTask(newBrokerItem, itemToRegister, player.getInventory().getKinahItem(), player.getObjectId());
		saveManager.add(bost);

		PacketSendUtility.sendPacket(player, new SM_BROKER_SERVICE(newBrokerItem, 0, registeredItemsCount));
	}

	public void showSellWindow(Player player, int itemUniqueId) {
		Item itemToRegister = player.getInventory().getItemByObjId(itemUniqueId);
		if (itemToRegister == null)
			return;
		LongSummaryStatistics priceStats = getRaceBrokerItems(player.getRace()).values().stream()
			.filter(item -> itemToRegister.getItemId() == item.getItemId())
			.mapToLong(BrokerItem::getPrice)
			.summaryStatistics();
		long lowestPrice = priceStats.getMin() == Long.MAX_VALUE ? 0 : priceStats.getMin();
		long highestPrice = priceStats.getMax() == Long.MIN_VALUE ? 0 : priceStats.getMax();
		PacketSendUtility.sendPacket(player, new SM_BROKER_SERVICE((byte) 0, itemUniqueId, lowestPrice, highestPrice));
	}

	public void showRegisteredItems(Player player) {
		Map<Integer, BrokerItem> brokerItems = getRaceBrokerItems(player.getRace());

		List<BrokerItem> registeredItems = new ArrayList<>();
		int playerId = player.getObjectId();

		for (BrokerItem item : brokerItems.values()) {
			if (item != null && item.getItem() != null && playerId == item.getSellerId())
				registeredItems.add(item);
		}

		PacketSendUtility.sendPacket(player, new SM_BROKER_SERVICE(registeredItems.toArray(new BrokerItem[registeredItems.size()])));
	}

	public boolean hasRegisteredItems(Player player) {
		Map<Integer, BrokerItem> brokerItems = getRaceBrokerItems(player.getRace());
		for (BrokerItem item : brokerItems.values()) {
			if (item != null && item.getItem() != null && player.getObjectId() == item.getSellerId())
				return true;
		}

		return false;
	}

	public void cancelRegisteredItem(Player player, int brokerItemId) {
		Map<Integer, BrokerItem> brokerItems = getRaceBrokerItems(player.getRace());
		BrokerItem brokerItem = brokerItems.get(brokerItemId);

		if (!PlayerRestrictions.canTrade(player)) {
			return;
		}
		if (brokerItem != null) {
			if (brokerItem.getSellerId() != player.getObjectId()) {
				log.info("[AUDIT] Player: {} tried to get item from broker that he doesn't own", player.getName());
				return;
			}
			if (player.getInventory().isFull(brokerItem.getItem().getItemTemplate().getExtraInventoryId())) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_EXCHANGE_FULL_INVENTORY());
				return;
			}
			synchronized (this) {
				player.getInventory().add(brokerItem.getItem(), ItemPacketService.ItemAddType.BROKER_RETURN);
				brokerItem.setPersistentState(PersistentState.DELETED);
				saveManager.add(new BrokerOpSaveTask(brokerItem));
				brokerItem.setIsCanceled(true);
				brokerItems.remove(brokerItemId);
				PacketSendUtility.sendPacket(player, new SM_BROKER_SERVICE((byte) 0, brokerItemId));
			}
		}
		showRegisteredItems(player);
	}

	public void showSettledItems(Player player, int startPageIndex) {
		int itemsPerPage = 9;
		List<BrokerItem> settledItems = getSettledItemsForPlayer(player.getRace(), player.getObjectId());
		List<BrokerItem> itemsToSend = settledItems.subList(itemsPerPage * startPageIndex, settledItems.size());
		SplitList<BrokerItem> itemSplitList = new DynamicServerPacketBodySplitList<>(itemsToSend, true, SM_BROKER_SERVICE.SETTLED_ITEMS_STATIC_BODY_SIZE,
			SM_BROKER_SERVICE.SETTLED_ITEMS_DYNAMIC_BODY_PART_SIZE_CALCULATOR);
		ListPart<BrokerItem> pagesToSend = itemSplitList.iterator().next(); // client only supports one packet worth of pages
		int lastFullPageIndex = pagesToSend.isLast() || pagesToSend.size() <= itemsPerPage ? pagesToSend.size() : pagesToSend.size() - pagesToSend.size() % itemsPerPage;
		List<BrokerItem> firstFullPages = pagesToSend.subList(0, lastFullPageIndex); // incomplete pages create gaps, so we trim sent items to full pages
		PacketSendUtility.sendPacket(player, new SM_BROKER_SERVICE(firstFullPages, settledItems.size(), startPageIndex, extractEarnedKinahForSoldItems(settledItems)));
	}

	private List<BrokerItem> getSettledItemsForPlayer(Race playerRace, int playerId) {
		Map<Integer, BrokerItem> settledItemsForRace = getRaceBrokerSettledItems(playerRace);
		if (settledItemsForRace == null)
			return Collections.emptyList();
		return settledItemsForRace.values().stream().filter(Objects::nonNull).filter(item -> item.getSellerId() == playerId).collect(Collectors.toList());
	}

	private long extractEarnedKinahForSoldItems(Collection<BrokerItem> items) {
		if (items == null || items.isEmpty())
			return 0;
		return items.stream().filter(Objects::nonNull).filter(BrokerItem::isSold).mapToLong(item -> item.getPrice() * item.getItemCount()).sum();
	}

	public long getEarnedKinahFromSoldItems(PlayerCommonData playerCommonData) {
		return getEarnedKinahFromSoldItems(playerCommonData.getRace(), playerCommonData.getPlayerObjId());
	}

	private long getEarnedKinahFromSoldItems(Race playerRace, int playerId) {
		return extractEarnedKinahForSoldItems(getSettledItemsForPlayer(playerRace, playerId));
	}

	public void settleAccount(Player player) {
		Race playerRace = player.getRace();
		Map<Integer, BrokerItem> brokerSettledItems = getRaceBrokerSettledItems(playerRace);
		List<BrokerItem> collectedItems = new ArrayList<>();
		int playerId = player.getObjectId();
		long kinahCollect = 0;
		boolean itemsLeft = false;

		for (BrokerItem item : brokerSettledItems.values()) {
			if (item.getSellerId() == playerId)
				collectedItems.add(item);
		}

		for (BrokerItem item : collectedItems) {
			if (item.isSold()) {
				boolean result = false;
				switch (playerRace) {
					case ASMODIANS:
						result = asmodianSettledItems.remove(item.getItemUniqueId()) != null;
						break;
					case ELYOS:
						result = elyosSettledItems.remove(item.getItemUniqueId()) != null;
						break;
				}

				if (result) {
					item.setPersistentState(PersistentState.DELETED);
					saveManager.add(new BrokerOpSaveTask(item));
					kinahCollect += item.getPrice() * item.getItemCount();
				}
			} else {
				if (item.getItem() != null) {
					Item resultItem = player.getInventory().add(item.getItem());
					if (resultItem != null) {
						boolean result = false;
						switch (playerRace) {
							case ASMODIANS:
								result = asmodianSettledItems.remove(item.getItemUniqueId()) != null;
								break;
							case ELYOS:
								result = elyosSettledItems.remove(item.getItemUniqueId()) != null;
								break;
						}
						if (result) {
							item.setPersistentState(PersistentState.DELETED);
							saveManager.add(new BrokerOpSaveTask(item));
						}
					} else
						itemsLeft = true;

				} else
					log.warn("Broker settled item missed. ObjID: " + item.getItemUniqueId());
			}
		}

		player.getInventory().increaseKinah(kinahCollect);

		showSettledItems(player, 0);

		if (!itemsLeft)
			PacketSendUtility.sendPacket(player, new SM_BROKER_SERVICE(false, 0));

	}

	private void checkExpiredItems() {
		long now = System.currentTimeMillis();
		for (Race race : Arrays.asList(Race.ASMODIANS, Race.ELYOS)) {
			Map<Integer, BrokerItem> brokerItems = getRaceBrokerItems(race);
			for (BrokerItem item : brokerItems.values()) {
				if (item != null && item.getExpireTime().getTime() <= now) {
					synchronized (this) {
						putToSettled(race, item, false);
						brokerItems.remove(item.getItemUniqueId());
					}
				}
			}
		}
	}

	public void onPlayerLogin(Player player) {
		List<BrokerItem> settledItemsForPlayer = getSettledItemsForPlayer(player.getRace(), player.getObjectId());
		if (!settledItemsForPlayer.isEmpty())
			PacketSendUtility.sendPacket(player, new SM_BROKER_SERVICE(true, extractEarnedKinahForSoldItems(settledItemsForPlayer)));
	}

	private BrokerPlayerCache getPlayerCache(Player player) {
		BrokerPlayerCache cacheEntry = playerBrokerCache.get(player.getObjectId());
		if (cacheEntry == null) {
			cacheEntry = new BrokerPlayerCache();
			playerBrokerCache.put(player.getObjectId(), cacheEntry);
		}
		return cacheEntry;
	}

	public void removePlayerCache(Player player) {
		playerBrokerCache.remove(player.getObjectId());
	}

	public void onPlayerDeleted(int playerId) {
		for (Race playerRace : Arrays.asList(Race.ELYOS, Race.ASMODIANS)) {
			Map<Integer, BrokerItem> brokerItems = getRaceBrokerItems(playerRace);
			if (brokerItems != null) {
				synchronized (brokerItems) {
					brokerItems.values().removeIf(brokerItem -> brokerItem.getSellerId() == playerId);
				}
			}
			brokerItems = getRaceBrokerSettledItems(playerRace);
			if (brokerItems != null) {
				synchronized (brokerItems) {
					brokerItems.values().removeIf(brokerItem -> brokerItem.getSellerId() == playerId);
				}
			}
		}
	}

	private int getPlayerMask(Player player) {
		return getPlayerCache(player).getBrokerMaskCache();
	}

	private BrokerItem[] getFilteredItems(Player player) {
		return getPlayerCache(player).getBrokerListCache();
	}

	/**
	 * Frequent running save task
	 */
	public static final class BrokerPeriodicTaskManager extends AbstractFIFOPeriodicTaskManager<BrokerOpSaveTask> {

		private static final String CALLED_METHOD_NAME = "brokerOperation()";

		public BrokerPeriodicTaskManager(int period) {
			super(period);
		}

		@Override
		protected void callTask(BrokerOpSaveTask task) {
			task.run();
		}

		@Override
		protected String getCalledMethodName() {
			return CALLED_METHOD_NAME;
		}

	}

	/**
	 * This class is used for storing all items in one shot after any broker operation
	 */
	public static final class BrokerOpSaveTask implements Runnable {

		private BrokerItem brokerItem;
		private Item item;
		private Item kinahItem;
		private int playerId;

		private BrokerOpSaveTask(BrokerItem brokerItem, Item item, Item kinahItem, int playerId) {
			this.brokerItem = brokerItem;
			this.item = item;
			this.kinahItem = kinahItem;
			this.playerId = playerId;
		}

		public BrokerOpSaveTask(BrokerItem brokerItem) {
			this.brokerItem = brokerItem;
		}

		@Override
		public void run() {
			// first save item for FK consistency
			if (item != null) {
				InventoryDAO.store(item, playerId);
				ItemStoneListDAO.save(List.of(item));
			}
			if (brokerItem != null)
				BrokerDAO.store(brokerItem);
			if (kinahItem != null)
				InventoryDAO.store(kinahItem, playerId);
		}

	}

	private static class SingletonHolder {

		protected static final BrokerService instance = new BrokerService();
	}

}
