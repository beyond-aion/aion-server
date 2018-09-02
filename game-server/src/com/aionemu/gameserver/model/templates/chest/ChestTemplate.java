package com.aionemu.gameserver.model.templates.chest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
