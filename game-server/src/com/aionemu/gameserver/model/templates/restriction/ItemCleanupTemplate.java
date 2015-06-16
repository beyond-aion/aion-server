package com.aionemu.gameserver.model.templates.restriction;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author KID
 */
@XmlRootElement(name = "item_restriction_cleanups")
@XmlAccessorType(XmlAccessType.NONE)
public class ItemCleanupTemplate {
	@XmlAttribute(name = "id", required = true)
	private int id;

	@XmlAttribute
	private byte trade = -1;
	@XmlAttribute
	private byte sell = -1;
	@XmlAttribute
	private byte wh = -1;
	@XmlAttribute
	private byte awh = -1;
	@XmlAttribute
	private byte lwh = -1;

	public byte resultTrade() {
		return trade;
	}
	
	public byte resultSell() {
		return sell;
	}
	
	public byte resultWH() {
		return wh;
	}
	
	public byte resultAccountWH() {
		return awh;
	}
	
	public byte resultLegionWH() {
		return lwh;
	}

	public int getId() {
		return id;
	}
}
