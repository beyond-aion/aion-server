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
}
