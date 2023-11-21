package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.goods.GoodsList;

/**
 * @author ATracer
 */
@XmlRootElement(name = "goodslists")
@XmlAccessorType(XmlAccessType.FIELD)
public class GoodsListData {

	@XmlElement(required = true)
	protected List<GoodsList> list;

	@XmlElement(name = "in_list")
	protected List<GoodsList> inList;

	@XmlElement(name = "purchase_list")
	protected List<GoodsList> purchaseList;

	@XmlTransient
	private final Map<Integer, GoodsList> goodsListData = new HashMap<>();
	@XmlTransient
	private final Map<Integer, GoodsList> goodsInListData = new HashMap<>();
	@XmlTransient
	private final Map<Integer, GoodsList> goodsPurchaseListData = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (GoodsList it : list) {
			goodsListData.put(it.getId(), it);
		}
		for (GoodsList it : inList) {
			goodsInListData.put(it.getId(), it);
		}
		for (GoodsList it : purchaseList) {
			goodsPurchaseListData.put(it.getId(), it);
		}
		list = inList = purchaseList = null;
	}

	public GoodsList getGoodsListById(int id) {
		return goodsListData.get(id);
	}

	public GoodsList getGoodsInListById(int id) {
		return goodsInListData.get(id);
	}

	public GoodsList getGoodsPurchaseListById(int id) {
		return goodsPurchaseListData.get(id);
	}

	/**
	 * @return goodListData.size()
	 */
	public int size() {
		return goodsListData.size() + goodsInListData.size() + goodsPurchaseListData.size();
	}
}
