package com.aionemu.gameserver.model.templates;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

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
