package com.aionemu.gameserver.model.ingameshop;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.ingameshop.InGameShopProperty;
import com.aionemu.gameserver.configs.main.InGameShopConfig;
import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.dao.InGameShopDAO;
import com.aionemu.gameserver.dao.InGameShopLogDAO;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.templates.mail.MailMessage;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MAIL_SERVICE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TOLL_INFO;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_ACCOUNT_TOLL_INFO;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_PREMIUM_CONTROL;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author KID, xTz
 */
public class InGameShopEn {

	private static InGameShopEn instance = new InGameShopEn();
	private final Logger log = LoggerFactory.getLogger("INGAMESHOP_LOG");

	public static InGameShopEn getInstance() {
		return instance;
	}

	private Map<Byte, List<IGItem>> items;
	private InGameShopProperty iGProperty;
	private AtomicInteger lastRequestId = new AtomicInteger(0);
	private ConcurrentHashMap<Integer, IGRequest> activeRequests;

	public InGameShopEn() {
		if (!InGameShopConfig.ENABLE_IN_GAME_SHOP) {
			log.info("InGameShop is disabled.");
			return;
		}
		iGProperty = InGameShopProperty.load();
		activeRequests = new ConcurrentHashMap<>();
		items = InGameShopDAO.loadInGameShopItems();
		log.info("Loaded with " + items.size() + " items.");
	}

	public InGameShopProperty getIGSProperty() {
		return iGProperty;
	}

	public void reload() {
		if (!InGameShopConfig.ENABLE_IN_GAME_SHOP) {
			log.info("InGameShop is disabled.");
			return;
		}
		iGProperty.clear();
		iGProperty = InGameShopProperty.load();
		items = InGameShopDAO.loadInGameShopItems();
		log.info("Loaded with " + items.size() + " items.");
	}

	public IGItem getIGItem(int id) {
		for (byte key : items.keySet()) {
			for (IGItem item : items.get(key)) {
				if (item.getObjectId() == id) {
					return item;
				}
			}
		}
		return null;
	}

	public Collection<IGItem> getItems(byte category) {
		if (!items.containsKey(category)) {
			return Collections.emptyList();
		}
		return this.items.get(category);
	}

	@SuppressWarnings("unchecked")
	public List<Integer> getTopSales(int subCategory, byte category) {
		byte max = 6;
		TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>(new DescFilter());
		if (!items.containsKey(category)) {
			return new ArrayList<>();
		}
		for (IGItem item : this.items.get(category)) {
			if (item.getSalesRanking() == 0)
				continue;

			if (subCategory != 2 && item.getSubCategory() != subCategory)
				continue;

			map.put(item.getSalesRanking(), item.getObjectId());
		}
		List<Integer> top = new ArrayList<>();
		byte cnt = 0;
		for (int objId : map.values()) {
			if (cnt <= max) {
				top.add(objId);
				cnt++;
			} else
				break;
		}
		map.clear();
		return top;
	}

	@SuppressWarnings("rawtypes")
	class DescFilter implements Comparator {

		@Override
		public int compare(Object o1, Object o2) {
			Integer i1 = (Integer) o1;
			Integer i2 = (Integer) o2;
			return -i1.compareTo(i2);
		}

	}

	public int getMaxList(byte subCategoryId, byte category) {
		int id = 0;
		if (!items.containsKey(category)) {
			return id;
		}
		for (IGItem item : items.get(category)) {
			if (item.getSubCategory() == subCategoryId)
				if (item.getList() > id)
					id = item.getList();
		}

		return id;
	}

	public void buyItemRequest(Player player, int itemObjId) {
		if (player.getInventory().isFull()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_DICE_INVEN_ERROR());
			return;
		}

		IGItem item = InGameShopEn.getInstance().getIGItem(itemObjId);
		IGRequest request = new IGRequest(lastRequestId.incrementAndGet(), player.getObjectId(), itemObjId);
		request.accountId = player.getClientConnection().getAccount().getId();
		if (LoginServer.getInstance().sendPacket(new SM_PREMIUM_CONTROL(request, item.getItemPrice())))
			activeRequests.put(request.requestId, request);
		if (LoggingConfig.LOG_INGAMESHOP)
			log.info("[INGAMESHOP] > " + player + " (" + player.getAccount() + ") is watching item:" + item.getItemId() + " cost " + item.getItemPrice() + " toll.");
	}

	public void giftItemRequest(Player player, String receiver, String message, int itemObjId) {
		if (receiver.equalsIgnoreCase(player.getName())) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INGAMESHOP_CANNOT_GIVE_TO_ME());
			return;
		}

		if (!InGameShopConfig.ALLOW_GIFTS) {
			PacketSendUtility.sendMessage(player, "Gifts are disabled.");
			return;
		}

		if (!PlayerDAO.isNameUsed(receiver)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INGAMESHOP_NO_USER_TO_GIFT());
			return;
		}

		PlayerCommonData recipientCommonData = PlayerDAO.loadPlayerCommonDataByName(receiver);
		if (recipientCommonData.getMailboxLetters() >= 100) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MAIL_MSG_RECIPIENT_MAILBOX_FULL(recipientCommonData.getName()));
			return;
		}

		if (!InGameShopConfig.ENABLE_GIFT_OTHER_RACE && !player.isStaff())
			if (player.getRace() != recipientCommonData.getRace()) {
				PacketSendUtility.sendPacket(player, new SM_MAIL_SERVICE(MailMessage.MAIL_IS_ONE_RACE_ONLY));
				return;
			}

		IGItem item = getIGItem(itemObjId);
		IGRequest request = new IGRequest(lastRequestId.incrementAndGet(), player.getObjectId(), receiver, message, itemObjId);
		request.accountId = player.getClientConnection().getAccount().getId();
		if (LoginServer.getInstance().sendPacket(new SM_PREMIUM_CONTROL(request, item.getItemPrice())))
			activeRequests.put(request.requestId, request);
	}

	public void addToll(Player player, long cnt) {
		if (InGameShopConfig.ENABLE_IN_GAME_SHOP) {
			IGRequest request = new IGRequest(lastRequestId.incrementAndGet(), player.getObjectId(), 0);
			request.accountId = player.getClientConnection().getAccount().getId();
			PacketSendUtility.sendMessage(player, "You received " + cnt + " Toll");
			if (LoginServer.getInstance().sendPacket(new SM_PREMIUM_CONTROL(request, cnt * -1)))
				activeRequests.put(request.requestId, request);
		} else {
			PacketSendUtility.sendMessage(player, "You can't add toll if ingameshop is disabled!");
		}
	}

	public void addToll(int playerId, long cnt) {
		if (InGameShopConfig.ENABLE_IN_GAME_SHOP) {
			IGRequest request = new IGRequest(lastRequestId.incrementAndGet(), playerId, 0);
			request.accountId = playerId;
			if (LoginServer.getInstance().sendPacket(new SM_PREMIUM_CONTROL(request, cnt * -1)))
				activeRequests.put(request.requestId, request);
		}
	}

	public boolean decreaseToll(Player player, long price) {
		if (LoginServer.getInstance().sendPacket(
			new SM_ACCOUNT_TOLL_INFO(player.getClientConnection().getAccount().getToll() - price, player.getAccount().getId()))) {
			player.getClientConnection().getAccount().setToll(player.getClientConnection().getAccount().getToll() - price);
			PacketSendUtility.sendPacket(player, new SM_TOLL_INFO(player.getClientConnection().getAccount().getToll()));
			return true;
		} else {
			PacketSendUtility.sendMessage(player, "ls communication error.");
			return false;
		}
	}

	public void finishRequest(int requestId, int result, long toll) {
		IGRequest request = this.activeRequests.get(requestId);
		if (request.requestId == requestId) {
			Player player = World.getInstance().getPlayer(request.playerId);
			if (player != null) {
				if (result == 1) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INGAMESHOP_ERROR());
				} else if (result == 2) {
					IGItem item = getIGItem(request.itemObjId);
					if (item == null) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INGAMESHOP_ERROR());
						log.error("player " + player.getName() + " requested " + request.itemObjId + " that was not exists in list.");
						return;
					}
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_INGAMESHOP_NOT_ENOUGH_CASH("Toll"));
					PacketSendUtility.sendPacket(player, new SM_TOLL_INFO(toll));
					if (LoggingConfig.LOG_INGAMESHOP)
						log.info("[INGAMESHOP] > " + player + " (" + player.getAccount() + ") has not bought item: " + item.getItemId() + " count: " + item.getItemCount() + " Cause: NOT ENOUGH TOLLS");
				} else if (result == 3) {
					// uses for lottery
					if (request.itemObjId == 0) {
						PacketSendUtility.sendPacket(player, new SM_TOLL_INFO(toll));
						player.getClientConnection().getAccount().setToll(toll);
						return;
					}

					IGItem item = getIGItem(request.itemObjId);
					if (item == null) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INGAMESHOP_ERROR());
						log.error("player " + player.getName() + " requested " + request.itemObjId + " that was not exists in list.");
						return;
					}

					if (request.gift) {
						SystemMailService.sendMail(player.getName(), request.receiver, "In Game Shop", request.message, item.getItemId(), item.getItemCount(), 0,
							LetterType.BLACKCLOUD);
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_INGAMESHOP_GIFT_SUCCESS());
						if (LoggingConfig.LOG_INGAMESHOP)
							log.info("[INGAMESHOP] > " + player + " (" + player.getAccount() + ") BUY ITEM: " + item.getItemId() + " COUNT: " + item.getItemCount() + " FOR PlayerName: " + request.receiver);
						if (LoggingConfig.LOG_INGAMESHOP_SQL)
							InGameShopLogDAO.log("GIFT", new Timestamp(System.currentTimeMillis()), player.getName(),
								player.getAccountName(), request.receiver, item.getItemId(), item.getItemCount(), item.getItemPrice());
					} else {
						ItemService.addItem(player, item.getItemId(), item.getItemCount());
						if (LoggingConfig.LOG_INGAMESHOP)
							log.info("[INGAMESHOP] > " + player + " (" + player.getAccount() + ") BUY ITEM: " + item.getItemId() + " COUNT: " + item.getItemCount());
						if (LoggingConfig.LOG_INGAMESHOP_SQL)
							InGameShopLogDAO.log("BUY", new Timestamp(System.currentTimeMillis()), player.getName(),
								player.getAccountName(), player.getName(), item.getItemId(), item.getItemCount(), item.getItemPrice());
						InventoryDAO.store(player);
					}
					item.increaseSales();
					InGameShopDAO.increaseSales(item.getObjectId(), item.getSalesRanking());
					PacketSendUtility.sendPacket(player, new SM_TOLL_INFO(toll));
					player.getClientConnection().getAccount().setToll(toll);
				} else if (result == 4) {
					player.getClientConnection().getAccount().setToll(toll);
					PacketSendUtility.sendPacket(player, new SM_TOLL_INFO(toll));
				}
			}

			activeRequests.remove(request.requestId);
		}
	}

}
