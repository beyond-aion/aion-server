package com.aionemu.gameserver.model.templates.siegelocation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Whoop
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SiegeMercenaryZone")
public class SiegeMercenaryZone {

	@XmlAttribute(name = "id")
	protected int id;
	@XmlAttribute(name = "costs")
	protected int costs;
	@XmlAttribute(name = "cooldown")
	protected int cooldown;
	@XmlAttribute(name = "msg_id")
	protected int msgId;
	@XmlAttribute(name = "announce_id")
	protected int announceId;

	public int getId() {
		return id;
	}

	public int getCosts() {
		return costs;
	}

	public int getCooldown() {
		return cooldown * 1000;
	}

	public int getMsgId() {
		return msgId;
	}

	public int getAnnounceId() {
		return announceId;
	}
}
