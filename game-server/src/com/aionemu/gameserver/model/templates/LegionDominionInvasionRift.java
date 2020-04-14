package com.aionemu.gameserver.model.templates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Sykra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LegionDominionInvasionRift")
public class LegionDominionInvasionRift {

	@XmlAttribute(name = "key_item_id", required = true)
	private int keyItemId;
	@XmlAttribute(name = "rift_id", required = true)
	private int riftId;

	public int getRiftId() {
		return riftId;
	}

	public int getKeyItemId() {
		return keyItemId;
	}
}
