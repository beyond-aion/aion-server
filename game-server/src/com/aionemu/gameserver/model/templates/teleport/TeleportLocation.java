package com.aionemu.gameserver.model.templates.teleport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author ATracer
 */
@XmlRootElement(name = "telelocation")
@XmlAccessorType(XmlAccessType.FIELD)
public class TeleportLocation {

	@XmlAttribute(name = "loc_id", required = true)
	private int locId;
	@XmlAttribute(name = "teleportid")
	private int teleportid = 0;
	@XmlAttribute(name = "price", required = true)
	private int price = 0;
	@XmlAttribute(name = "pricePvp")
	private int pricePvp = 0;
	@XmlAttribute(name = "required_quest")
	private int required_quest = 0;

	@XmlAttribute(name = "type", required = true)
	private TeleportType type;

	public int getLocId() {
		return locId;
	}

	public int getTeleportId() {
		return teleportid;
	}

	public int getPrice() {
		return price;
	}

	public int getPricePvp() {
		return pricePvp;
	}

	public int getRequiredQuest() {
		return required_quest;
	}

	public TeleportType getType() {
		return type;
	}
}
