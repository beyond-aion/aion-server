package com.aionemu.gameserver.model.templates.goods;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.limiteditems.LimitedItem;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GoodsList")
public class GoodsList {

	@XmlElement(name = "item")
	private List<Item> items;
	@XmlAttribute(name = "id")
	private int id;
	@XmlAttribute(name = "legion_lvl")
	private int legionLevel;
	@XmlAttribute(name = "advertise")
	private int advertiseStringId;
	@XmlAttribute(name = "gossip")
	private int gossipStringId;
	@XmlElement(name = "salestime")
	private String salesTime;

	private List<Integer> itemIdList;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		itemIdList = new ArrayList<>();
		if (items == null)
			return;

		for (Item item : items) {
			itemIdList.add(item.getId());
		}
	}

	/**
	 * return the limitedItems.
	 */
	public List<LimitedItem> getLimitedItems() {
		List<LimitedItem> limitedItems = new ArrayList<>();
		if (items != null) {
			for (Item item : items) {
				if (item.getBuyLimit() != null && item.getSellLimit() != null) {
					limitedItems.add(new LimitedItem(item.getId(), item.getSellLimit(), item.getBuyLimit(), salesTime));
				}
			}
		}
		return limitedItems;
	}

	/**
	 * Gets the value of the id property.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the itemIdList
	 */
	public List<Integer> getItemIdList() {
		return itemIdList;
	}

	/**
	 * @return the legionLevel
	 */
	public int getLegionLevel() {
		return legionLevel;
	}

	/**
	 * @return the advertiseStringId
	 */
	public int getAdvertiseStringId() {
		return advertiseStringId;
	}

	/**
	 * @return the gossipStringId
	 */
	public int getGossipStringId() {
		return gossipStringId;
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * <p>
	 * The following schema fragment specifies the expected content contained within this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}int" />
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "")
	public static class Item {

		@XmlAttribute
		private int id;
		@XmlAttribute(name = "sell_limit")
		private Integer sellLimit;
		@XmlAttribute(name = "buy_limit")
		private Integer buyLimit;

		/**
		 * Gets the value of the id property.
		 */
		public int getId() {
			return id;
		}

		/**
		 * return sellLimit.
		 */
		public Integer getSellLimit() {
			return sellLimit;
		}

		/**
		 * return buyLimit.
		 */
		public Integer getBuyLimit() {
			return buyLimit;
		}
	}
}
