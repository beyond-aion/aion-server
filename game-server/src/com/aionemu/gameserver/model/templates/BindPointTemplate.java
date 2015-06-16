package com.aionemu.gameserver.model.templates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author avol
 */
@XmlRootElement(name = "bind_points")
@XmlAccessorType(XmlAccessType.NONE)
public class BindPointTemplate {

	@XmlAttribute(name = "name", required = true)
	private String name;

	@XmlAttribute(name = "npcid")
	private int npcId;

	@XmlAttribute(name = "price")
	private int price = 0;

	public String getName() {
		return name;
	}

	public int getNpcId() {
		return npcId;
	}

	public int getPrice() {
		return price;
	}
}
