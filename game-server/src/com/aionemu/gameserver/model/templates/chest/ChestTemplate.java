package com.aionemu.gameserver.model.templates.chest;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Chest")
public class ChestTemplate {

	@XmlAttribute(name = "npc_id")
	protected int npcId;
	@XmlElement(name = "key_item")
	protected List<KeyItem> keyItems;

	public int getNpcId() {
		return npcId;
	}

	public List<KeyItem> getKeyItems() {
		return keyItems;
	}
}
