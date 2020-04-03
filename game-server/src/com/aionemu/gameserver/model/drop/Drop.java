package com.aionemu.gameserver.model.drop;

import javax.xml.bind.Unmarshaller;
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
	private int minAmount = 1;
	@XmlAttribute(name = "max_amount")
	private int maxAmount;
	@XmlAttribute(name = "chance")
	private float chance = 100;
	@XmlAttribute(name = "each_member")
	private boolean eachMember = false;

	/**
	 * private constructor for deserialization
	 */
	private Drop() {
	}

	public Drop(int itemId, int minAmount, int maxAmount, float chance) {
		this.itemId = itemId;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
		this.chance = chance;
		afterUnmarshal(null, null);
	}

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (chance <= 0)
			throw new IllegalArgumentException("chance (" + chance + ") for drop " + itemId + " must be greater than zero");
		if (minAmount <= 0)
			throw new IllegalArgumentException("minAmount (" + minAmount + ") for drop " + itemId + " must be greater than zero");
		if (maxAmount == 0)
			maxAmount = minAmount;
		else if (maxAmount < minAmount)
			throw new IllegalArgumentException("maxAmount (" + maxAmount + ") for drop " + itemId + " must be greater than minAmount (" + minAmount + ")");
	}

	public int getItemId() {
		return itemId;
	}

	public int getMinAmount() {
		return minAmount;
	}

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
