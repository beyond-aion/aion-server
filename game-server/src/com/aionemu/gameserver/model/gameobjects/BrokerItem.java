package com.aionemu.gameserver.model.gameobjects;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.broker.BrokerRace;

/**
 * @author kosyachok
 */
public class BrokerItem implements Comparable<BrokerItem>, Persistable {

	private final Item item;
	private final int itemId;
	private final int itemUniqueId;
	private long itemCount;
	private final String itemCreator;
	private final long price;
	private final int sellerId;
	private final BrokerRace itemBrokerRace;
	private boolean isSold, isCanceled;
	private boolean isSettled;
	private final Timestamp expireTime;
	private Timestamp settleTime;
	private final boolean splittingAvailable;
	private long averagePrice;

	PersistentState state;

	public BrokerItem(Item item, long price, int sellerId, boolean splittingAvailable, BrokerRace itemBrokerRace) {
		this.item = item;
		this.itemId = item.getItemTemplate().getTemplateId();
		this.itemUniqueId = item.getObjectId();
		this.itemCount = item.getItemCount();
		this.itemCreator = item.getItemCreator();
		this.price = price;
		this.sellerId = sellerId;
		this.itemBrokerRace = itemBrokerRace;
		this.isSold = false;
		this.isSettled = false;
		this.splittingAvailable = splittingAvailable;
		this.expireTime = new Timestamp(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(CustomConfig.BROKER_REGISTRATION_EXPIRATION_DAYS));
		this.expireTime.setNanos(0); // db queries by this timestamp but doesn't store fractional seconds
		this.settleTime = new Timestamp(System.currentTimeMillis());
		this.state = PersistentState.NEW;
	}

	public BrokerItem(Item item, int itemId, int itemUniqueId, long itemCount, String itemCreator, long price, int sellerId, BrokerRace itemBrokerRace,
		boolean isSold, boolean isSettled, Timestamp expireTime, Timestamp settleTime, boolean splittingAvailable) {
		this.item = item;
		this.itemId = itemId;
		this.itemUniqueId = itemUniqueId;
		this.itemCount = itemCount;
		this.itemCreator = itemCreator;
		this.price = price;
		this.sellerId = sellerId;
		this.itemBrokerRace = itemBrokerRace;
		this.isSold = isSold;
		this.isSettled = isSettled;
		this.expireTime = expireTime;
		this.expireTime.setNanos(0); // db queries by this timestamp but doesn't store fractional seconds
		this.settleTime = settleTime;
		this.splittingAvailable = splittingAvailable;
		this.state = PersistentState.NOACTION;
	}

	/**
	 * @return itemCreator
	 */
	public String getItemCreator() {
		return itemCreator == null ? "" : itemCreator;
	}

	public Item getItem() {
		return item;
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	public void setIsCanceled(boolean isCanceled) {
		this.isCanceled = isCanceled;
	}

	public void removeItem() {
		// this.item = null;
		this.isSold = true;
		this.isSettled = true;
		this.settleTime = new Timestamp(System.currentTimeMillis());
	}

	public int getItemId() {
		return itemId;
	}

	public int getItemUniqueId() {
		return itemUniqueId;
	}

	/**
	 * @return
	 */
	public long getPrice() {
		return price;
	}

	public int getSellerId() {
		return sellerId;
	}

	/**
	 * @return
	 */
	public BrokerRace getItemBrokerRace() {
		return itemBrokerRace;
	}

	/**
	 * @return
	 */
	public boolean isSold() {
		return this.isSold;
	}

	@Override
	@SuppressWarnings("fallthrough")
	public void setPersistentState(PersistentState persistentState) {
		switch (persistentState) {
			case DELETED:
				if (this.state == PersistentState.NEW)
					this.state = PersistentState.NOACTION;
				else
					this.state = PersistentState.DELETED;
				break;
			case UPDATE_REQUIRED:
				if (this.state == PersistentState.NEW)
					break;
			default:
				this.state = persistentState;
		}

	}

	@Override
	public PersistentState getPersistentState() {
		return state;
	}

	public boolean isSettled() {
		return isSettled;
	}

	public void setSettled() {
		this.isSettled = true;
		this.settleTime = new Timestamp(System.currentTimeMillis());
	}

	public Timestamp getExpireTime() {
		return expireTime;
	}

	public Timestamp getSettleTime() {
		return settleTime;
	}

	public long getItemCount() {
		return itemCount;
	}

	public void decreaseItemCount(long value) {
		this.itemCount -= value;
		this.item.decreaseItemCount(value);
	}

	/**
	 * @return item level according to template
	 */
	private int getItemLevel() {
		return item.getItemTemplate().getLevel();
	}

	/**
	 * @return price for one piece
	 */
	private long getPiecePrice() {
		return getPrice() / getItemCount();
	}

	/**
	 * @return name of the item
	 */
	private String getItemName() {
		return item.getItemName();
	}

	public boolean isSplittingAvailable() {
		return splittingAvailable;
	}

	public long getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(long averagePrice) {
		this.averagePrice = averagePrice;
	}

	/**
	 * Default sorting: using itemUniqueId
	 */
	@Override
	public int compareTo(BrokerItem o) {
		return itemUniqueId > o.getItemUniqueId() ? 1 : -1;
	}

	/**
	 * Sorting using price of item
	 */
	static Comparator<BrokerItem> NAME_SORT_ASC = new Comparator<BrokerItem>() {

		@Override
		public int compare(BrokerItem o1, BrokerItem o2) {
			if (o1 == null || o2 == null)
				return comparePossiblyNull(o1, o2);
			return o1.getItemName().compareTo(o2.getItemName());
		}
	};

	static Comparator<BrokerItem> NAME_SORT_DESC = new Comparator<BrokerItem>() {

		@Override
		public int compare(BrokerItem o1, BrokerItem o2) {
			if (o1 == null || o2 == null)
				return comparePossiblyNull(o1, o2);
			return o1.getItemName().compareTo(o2.getItemName());
		}
	};

	/**
	 * Sorting using price of item
	 */
	static Comparator<BrokerItem> PRICE_SORT_ASC = new Comparator<BrokerItem>() {

		@Override
		public int compare(BrokerItem o1, BrokerItem o2) {
			if (o1 == null || o2 == null)
				return comparePossiblyNull(o1, o2);
			if (o1.getPrice() == o2.getPrice())
				return 0;
			return o1.getPrice() > o2.getPrice() ? 1 : -1;
		}
	};

	static Comparator<BrokerItem> PRICE_SORT_DESC = new Comparator<BrokerItem>() {

		@Override
		public int compare(BrokerItem o1, BrokerItem o2) {
			if (o1 == null || o2 == null)
				return comparePossiblyNull(o1, o2);
			if (o1.getPrice() == o2.getPrice())
				return 0;
			return o1.getPrice() > o2.getPrice() ? -1 : 1;
		}
	};

	/**
	 * Sorting using piece price of item
	 */
	static Comparator<BrokerItem> PIECE_PRICE_SORT_ASC = new Comparator<BrokerItem>() {

		@Override
		public int compare(BrokerItem o1, BrokerItem o2) {
			if (o1 == null || o2 == null)
				return comparePossiblyNull(o1, o2);
			if (o1.getPiecePrice() == o2.getPiecePrice())
				return 0;
			return o1.getPiecePrice() > o2.getPiecePrice() ? 1 : -1;
		}
	};

	static Comparator<BrokerItem> PIECE_PRICE_SORT_DESC = new Comparator<BrokerItem>() {

		@Override
		public int compare(BrokerItem o1, BrokerItem o2) {
			if (o1 == null || o2 == null)
				return comparePossiblyNull(o1, o2);
			if (o1.getPiecePrice() == o2.getPiecePrice())
				return 0;
			return o1.getPiecePrice() > o2.getPiecePrice() ? -1 : 1;
		}
	};

	/**
	 * Sorting using level of item
	 */
	static Comparator<BrokerItem> LEVEL_SORT_ASC = new Comparator<BrokerItem>() {

		@Override
		public int compare(BrokerItem o1, BrokerItem o2) {
			if (o1 == null || o2 == null)
				return comparePossiblyNull(o1, o2);
			if (o1.getItemLevel() == o2.getItemLevel())
				return 0;
			return o1.getItemLevel() > o2.getItemLevel() ? 1 : -1;
		}
	};

	static Comparator<BrokerItem> LEVEL_SORT_DESC = new Comparator<BrokerItem>() {

		@Override
		public int compare(BrokerItem o1, BrokerItem o2) {
			if (o1 == null || o2 == null)
				return comparePossiblyNull(o1, o2);
			if (o1.getItemLevel() == o2.getItemLevel())
				return 0;
			return o1.getItemLevel() > o2.getItemLevel() ? -1 : 1;
		}
	};

	private static <T extends Comparable<T>> int comparePossiblyNull(T aThis, T aThat) {
		int result = 0;
		if (aThis == null && aThat != null) {
			result = -1;
		} else if (aThis != null && aThat == null) {
			result = 1;
		}
		return result;
	}

	/**
	 * 1 - by name;<br>
	 * 2 - by level;<br>
	 * 4 - by totalPrice;<br>
	 * 6 - by price for piece (Math.round(item.getPrice() / item.getItemCount))<br>
	 * 
	 * @param sortType
	 * @return
	 */
	public static Comparator<BrokerItem> getComparatoryByType(byte sortType) {
		switch (sortType) {
			case 0:
				return NAME_SORT_ASC;
			case 1:
				return NAME_SORT_DESC;
			case 2:
				return LEVEL_SORT_ASC;
			case 3:
				return LEVEL_SORT_DESC;
			case 4:
				return PRICE_SORT_ASC;
			case 5:
				return PRICE_SORT_DESC;
			case 6:
				return PIECE_PRICE_SORT_ASC;
			case 7:
				return PIECE_PRICE_SORT_DESC;
			default:
				throw new IllegalArgumentException("Illegal sort type for broker items");
		}
	}
}
