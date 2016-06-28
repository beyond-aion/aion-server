package com.aionemu.gameserver.model.templates.event;

import java.time.ZonedDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.datatype.XMLGregorianCalendar;

import com.aionemu.gameserver.model.AttendType;

/**
 * @author Alcapwnd
 */
@XmlRootElement(name = "login_event")
@XmlAccessorType(XmlAccessType.NONE)
public class AtreianPassport {

	@XmlAttribute(name = "id", required = true)
	private int id;
	@XmlAttribute(name = "name")
	private String name = "";
	@XmlAttribute(name = "active", required = true)
	private int active;
	@XmlAttribute(name = "period_start", required = true)
	@XmlSchemaType(name = "dateTime")
	protected XMLGregorianCalendar pStart;
	@XmlAttribute(name = "period_end", required = true)
	@XmlSchemaType(name = "dateTime")
	protected XMLGregorianCalendar pEnd;
	@XmlAttribute(name = "attend_type", required = true)
	private AttendType attendType;
	@XmlAttribute(name = "attend_num")
	private int attendNum;
	@XmlAttribute(name = "reward_item", required = true)
	private int rewardItem;
	@XmlAttribute(name = "reward_item_num", required = true)
	private int rewardItemNum = 1;
	@XmlAttribute(name = "reward_item_expire")
	private int rewardItemExpire;
	@XmlTransient
	private int rewardId = 0;

	public int getId() {
		return id;
	}

	public int getActive() {
		return active;
	}

	public String getName() {
		return name;
	}

	public ZonedDateTime getPeriodStart() {
		return pStart.toGregorianCalendar().toZonedDateTime();
	}

	public ZonedDateTime getPeriodEnd() {
		return pEnd.toGregorianCalendar().toZonedDateTime();
	}

	public AttendType getAttendType() {
		return attendType;
	}

	public int getAttendNum() {
		return attendNum;
	}

	public int getRewardItem() {
		return rewardItem;
	}

	public int getRewardItemNum() {
		return rewardItemNum;
	}

	public int getRewardItemExpire() {
		return rewardItemExpire;
	}
}
