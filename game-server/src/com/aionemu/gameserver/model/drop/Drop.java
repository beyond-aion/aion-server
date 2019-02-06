package com.aionemu.gameserver.model.drop;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author MrPoke
 */
@XmlRootElement(name = "drop")
@XmlAccessorType(XmlAccessType.FIELD)
public class Drop {

	@XmlAttribute(name = "item_id")
	private int itemId;
	@XmlAttribute(name = "min_amount")
	private int minAmount;
	@XmlAttribute(name = "max_amount")
	private int maxAmount;
	@XmlAttribute(name = "chance")
	private float chance;
	@XmlAttribute(name = "each_member")
	private boolean eachMember = false;

	public Drop(int itemId, int minAmount, int maxAmount, float chance) {
		this.itemId = itemId;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
		this.chance = chance;
	}

	public Drop() {
	}

	/**
	 * Gets the value of the itemId property.
	 */
	public int getItemId() {
		return itemId;
	}

	/**
	 * Gets the value of the minAmount property.
	 */
	public int getMinAmount() {
		return minAmount;
	}

	/**
	 * Gets the value of the maxAmount property.
	 */
	public int getMaxAmount() {
		return maxAmount;
	}

	public float getChance() {
		return chance;
	}

	public Boolean isEachMember() {
		return eachMember;
	}

	@Override
	public String toString() {
		return "Drop [itemId=" + itemId + ", minAmount=" + minAmount + ", maxAmount=" + maxAmount + ", chance=" + chance + ", eachMember=" + eachMember + "]";
	}

}
