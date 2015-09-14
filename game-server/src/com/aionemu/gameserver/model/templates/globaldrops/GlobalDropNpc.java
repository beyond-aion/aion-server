package com.aionemu.gameserver.model.templates.globaldrops;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author AionCool
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropNpc")
public class GlobalDropNpc {

	@XmlAttribute(name = "npc_id", required = true)
	protected int npcId;

	public int getNpcId() {
		return npcId;
	}

	public void setNpcId(int value) {
		this.npcId = value;
	}

}
