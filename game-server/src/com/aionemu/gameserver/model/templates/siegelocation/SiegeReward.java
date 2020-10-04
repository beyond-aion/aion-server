package com.aionemu.gameserver.model.templates.siegelocation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Source
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SiegeReward")
public class SiegeReward {

	@XmlAttribute(name = "top", required = true)
	protected int top;
	@XmlAttribute(name = "item_id")
	protected int itemId;
	@XmlAttribute(name = "item_count")
	protected int itemCount;
	@XmlAttribute(name = "item_id_defeat")
	protected int itemIdDefeat;
	@XmlAttribute(name = "item_count_defeat")
	protected int itemCountDefeat;
	@XmlAttribute(name = "gp_win")
	protected int gpWin;
	@XmlAttribute(name = "gp_defeat")
	protected int gpDefeat;

	public int getTop() {
		return top;
	}

	public int getItemId() {
		return itemId;
	}

	public int getItemCount() {
		return itemCount;
	}

	public int getGpForWin() {
		return gpWin;
	}

	public int getGpForDefeat() {
		return gpDefeat;
	}

	public int getItemIdDefeat() {
		return itemIdDefeat;
	}

	public int getItemCountDefeat() {
		return itemCountDefeat;
	}

	public boolean hasItemRewardsForWin() {
		return itemId > 0 && itemCount > 0;
	}

	public boolean hasItemRewardsForDefeat() {
		return itemIdDefeat > 0 && itemCountDefeat > 0;
	}
}
