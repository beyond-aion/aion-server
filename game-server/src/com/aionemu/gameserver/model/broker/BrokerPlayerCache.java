package com.aionemu.gameserver.model.broker;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.BrokerItem;

/**
 * @author ATracer
 */
public class BrokerPlayerCache {

	private BrokerItem[] brokerListCache = new BrokerItem[0];
	private int brokerMaskCache;
	private byte brokerSoftTypeCache;
	private int brokerStartPageCache;
	private List<Integer> itemList = new ArrayList<>();

	/**
	 * @return the brokerListCache
	 */
	public BrokerItem[] getBrokerListCache() {
		return brokerListCache;
	}

	/**
	 * @param brokerListCache
	 *          the brokerListCache to set
	 */
	public void setBrokerListCache(BrokerItem[] brokerListCache) {
		this.brokerListCache = brokerListCache;
	}

	/**
	 * @return the brokerMaskCache
	 */
	public int getBrokerMaskCache() {
		return brokerMaskCache;
	}

	/**
	 * @param brokerMaskCache
	 *          the brokerMaskCache to set
	 */
	public void setBrokerMaskCache(int brokerMaskCache) {
		this.brokerMaskCache = brokerMaskCache;
	}

	/**
	 * @return the brokerSoftTypeCache
	 */
	public byte getBrokerSortTypeCache() {
		return brokerSoftTypeCache;
	}

	/**
	 * @param brokerSoftTypeCache
	 *          the brokerSoftTypeCache to set
	 */
	public void setBrokerSortTypeCache(byte brokerSoftTypeCache) {
		this.brokerSoftTypeCache = brokerSoftTypeCache;
	}

	/**
	 * @return the brokerStartPageCache
	 */
	public int getBrokerStartPageCache() {
		return brokerStartPageCache;
	}

	/**
	 * @param the
	 *          getSearchItemList
	 */
	public List<Integer> getSearchItemList() {
		if (this.itemList == null)
			return null;
		return this.itemList;
	}

	/**
	 * @param brokerStartPageCache
	 *          the brokerStartPageCache to set
	 */
	public void setBrokerStartPageCache(int brokerStartPageCache) {
		this.brokerStartPageCache = brokerStartPageCache;
	}

	/**
	 * @param setSearchItemsList
	 *          the searched item list to set
	 */
	public void setSearchItemsList(List<Integer> itemList) {
		this.itemList = itemList;
	}
}
