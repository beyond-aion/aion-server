package com.aionemu.gameserver.model.templates.arcadeupgrade;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ginho1
 */
@XmlType(name = "ArcadeTabItemList")
public class ArcadeTabItemList {

	@XmlAttribute(name = "item_id")
	protected int item_id;
	@XmlAttribute(name = "normalcount")
	protected int normalcount;
	@XmlAttribute(name = "frenzycount")
	protected int frenzycount;

	public final int getItemId() {
		return item_id;
	}

	public final int getNormalCount() {
		return normalcount;
	}

	public final int getFrenzyCount() {
		return frenzycount;
	}
}