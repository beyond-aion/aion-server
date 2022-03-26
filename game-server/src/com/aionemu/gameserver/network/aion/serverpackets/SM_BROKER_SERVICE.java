package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.aionemu.gameserver.model.gameobjects.BrokerItem;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.iteminfo.EnchantInfoBlobEntry;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob;
import com.aionemu.gameserver.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;
import com.aionemu.gameserver.services.player.PlayerService;

/**
 * @author IlBuono, kosyachok
 */
public class SM_BROKER_SERVICE extends AionServerPacket {

	public static final int SETTLED_ITEMS_STATIC_BODY_SIZE = 18;
	public static final Function<BrokerItem, Integer> SETTLED_ITEMS_DYNAMIC_BODY_PART_SIZE_CALCULATOR = 
			item -> 32 + EnchantInfoBlobEntry.SIZE + (item.getItemCreator() == null ? 2 : item.getItemCreator().length() * 2 + 2);

	private enum BrokerPacketType {
		SEARCHED_ITEMS(0),
		REGISTERED_ITEMS(1),
		BUY_ITEMS(2),
		REGISTER_ITEM(3),
		CANCEL_REGISTERED_ITEM(4),
		SHOW_SETTLED_ICON(5),
		SETTLED_ITEMS(5),
		REMOVE_SETTLED_ICON(6),
		SHOW_SELL_WINDOW(7);

		private int id;

		BrokerPacketType(int id) {
			this.id = id;
		}

		private int getId() {
			return id;
		}
	}

	private BrokerPacketType type;
	private BrokerItem[] brokerItems;
	private int itemsCount;
	private int startPage;
	private int message;
	private int totalItemCount, pageIndex;
	private long settledKinah, currentLow, currentHigh;
	private int itemId;
	private byte unk;

	public SM_BROKER_SERVICE(BrokerItem brokerItem, int message, int itemsCount) {
		this.type = BrokerPacketType.REGISTER_ITEM;
		this.brokerItems = new BrokerItem[] { brokerItem };
		this.message = message;
		this.itemsCount = itemsCount;
	}

	public SM_BROKER_SERVICE(int message) {
		this.type = BrokerPacketType.REGISTER_ITEM;
		this.message = message;
	}

	public SM_BROKER_SERVICE(BrokerItem[] brokerItems) {
		this.type = BrokerPacketType.REGISTERED_ITEMS;
		this.brokerItems = brokerItems;
	}

	public SM_BROKER_SERVICE(List<BrokerItem> brokerItems, int totalItemCount, int pageIndex, long settledKinah) {
		this.type = BrokerPacketType.SETTLED_ITEMS;
		this.totalItemCount = totalItemCount;
		this.pageIndex = pageIndex;
		this.brokerItems = brokerItems.toArray(BrokerItem[]::new);
		this.settledKinah = settledKinah;
	}

	public SM_BROKER_SERVICE(BrokerItem[] brokerItems, int itemsCount, int startPage) {
		this.type = BrokerPacketType.SEARCHED_ITEMS;
		this.brokerItems = brokerItems;
		this.itemsCount = itemsCount;
		this.startPage = startPage;
	}

	public SM_BROKER_SERVICE(boolean showSettledIcon, long settledKinah) {
		this.type = showSettledIcon ? BrokerPacketType.SHOW_SETTLED_ICON : BrokerPacketType.REMOVE_SETTLED_ICON;
		this.settledKinah = settledKinah;
	}

	public SM_BROKER_SERVICE(byte unk, int itemId) {
		this.type = BrokerPacketType.CANCEL_REGISTERED_ITEM;
		this.itemId = itemId;
		this.unk = unk;
	}

	public SM_BROKER_SERVICE(byte unk, int itemId, long currentLow, long currentHigh) {
		this.type = BrokerPacketType.SHOW_SELL_WINDOW;
		this.unk = unk;
		this.itemId = itemId;
		this.currentLow = currentLow;
		this.currentHigh = currentHigh;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		switch (type) {
			case SEARCHED_ITEMS:
				writeSearchedItems();
				break;
			case REGISTERED_ITEMS:
				writeRegisteredItems();
				break;
			case REGISTER_ITEM:
				writeRegisterItem();
				break;
			case CANCEL_REGISTERED_ITEM:
				writeCancelRegisteredItem();
				break;
			case SHOW_SETTLED_ICON:
				writeShowSettledIcon();
				break;
			case REMOVE_SETTLED_ICON:
				writeRemoveSettledIcon();
				break;
			case SETTLED_ITEMS:
				writeShowSettledItems();
				break;
			case SHOW_SELL_WINDOW:
				writeShowSellWindow();
				break;
		}

	}

	private void writeCancelRegisteredItem() {
		writeC(type.getId());
		writeC(unk);
		writeD(itemId);
	}

	private void writeSearchedItems() {
		writeC(type.getId());
		writeD(itemsCount);
		writeC(0);
		writeH(startPage);
		writeH(brokerItems.length > 36 ? 36 : brokerItems.length);
		int counter = 0;
		for (BrokerItem item : brokerItems) {
			if (counter < 36) {
				writeItemInfo(item);
				counter++;
			}
		}
	}

	private void writeRegisteredItems() {
		writeC(type.getId());
		writeD(0x00);
		writeH(brokerItems.length); // you can register a max of 15 items, so 0x0F
		for (BrokerItem brokerItem : brokerItems) {
			writeRegisteredItemInfo(brokerItem);
		}
	}

	private void writeRegisterItem() {
		writeC(type.getId());
		writeC(message);
		if (message == 0) {
			writeC(itemsCount + 1); // item pos in list
			BrokerItem itemForRegistration = brokerItems[0];
			writeRegisteredItemInfo(itemForRegistration);
		} else {
			writeB(new byte[174]);
			writeH(255); // right after creatorName string
			writeB(new byte[7]);
		}
	}

	private void writeShowSettledIcon() {
		writeC(type.getId());
		writeQ(settledKinah);
		writeD(0x00);
		writeH(0x00);
		writeH(0x01);
		writeC(0x00);
	}

	private void writeRemoveSettledIcon() {
		writeH(type.getId());
	}

	private void writeShowSettledItems() {
		writeC(type.getId());
		writeQ(settledKinah);
		writeD(totalItemCount); // total item count to determine total page count
		writeH(pageIndex); // zero-based index of the currently selected page
		writeC(0); // 1 clears the list (no items must be sent)
		writeH(brokerItems.length); // items sent in this packet (client will request items of unsent pages when needed)
		for (BrokerItem settledItem : brokerItems) {
			writeD(settledItem.getItemId());
			writeQ(settledItem.isSold() ? settledItem.getPrice() * settledItem.getItemCount() : 0);
			writeQ(settledItem.getItemCount());
			writeQ(settledItem.getItemCount());
			writeD((int) (settledItem.getSettleTime().getTime() / 60000));
			if (settledItem.getItem() == null) // sold items are null because they are not in item_location 126 (broker) anymore
				writeB(new byte[EnchantInfoBlobEntry.SIZE]);
			else
				EnchantInfoBlobEntry.writeInfo(getBuf(), settledItem.getItem());
			writeS(settledItem.getItemCreator());
		}
	}

	private void writeShowSellWindow() {
		writeC(type.getId());
		writeC(unk);
		writeD(itemId);
		writeD(0);
		writeD(0);
		writeC(3);// 7dayAverage
		writeQ(currentLow);// currentLow
		writeQ(currentHigh);// currentHigh
	}

	private void writeRegisteredItemInfo(BrokerItem brokerItem) {
		Item item = brokerItem.getItem();

		writeD(brokerItem.getItemUniqueId());
		writeD(brokerItem.getItemId());
		writeQ(brokerItem.getPrice() * brokerItem.getItemCount());
		writeQ(item.getItemCount());
		writeQ(item.getItemCount());
		int daysLeft = (int) TimeUnit.MILLISECONDS.toDays(brokerItem.getExpireTime().getTime() - System.currentTimeMillis());
		writeC(daysLeft);

		EnchantInfoBlobEntry.writeInfo(getBuf(), item);
		// ItemInfoBlob.newBlobEntry(ItemBlobType.PREMIUM_OPTION, null, item).writeThisBlob(getBuf());

		writeS(brokerItem.getItemCreator());
		writeH(0);
		writeC(0);
		ItemInfoBlob.newBlobEntry(ItemBlobType.POLISH_INFO, null, item).writeThisBlob(getBuf());
		ItemInfoBlob.newBlobEntry(ItemBlobType.WRAP_INFO, null, item).writeThisBlob(getBuf());
		writeC(brokerItem.isSplittingAvailable() ? 1 : 0);
	}

	private void writeItemInfo(BrokerItem brokerItem) {
		Item item = brokerItem.getItem();

		writeD(item.getObjectId());
		writeD(item.getItemTemplate().getTemplateId());
		writeQ(brokerItem.getPrice() * brokerItem.getItemCount());
		writeQ(brokerItem.getAveragePrice()); // AWR price
		writeQ(item.getItemCount());

		EnchantInfoBlobEntry.writeInfo(getBuf(), item);
		// ItemInfoBlob.newBlobEntry(ItemBlobType.PREMIUM_OPTION, null, item).writeThisBlob(getBuf());

		writeS(PlayerService.getPlayerName(brokerItem.getSellerId()));
		writeS(brokerItem.getItemCreator()); // creator
		writeH(0);
		writeC(0);
		ItemInfoBlob.newBlobEntry(ItemBlobType.POLISH_INFO, null, item).writeThisBlob(getBuf());
		ItemInfoBlob.newBlobEntry(ItemBlobType.WRAP_INFO, null, item).writeThisBlob(getBuf());
		writeC(brokerItem.isSplittingAvailable() ? 1 : 0);
	}
}
