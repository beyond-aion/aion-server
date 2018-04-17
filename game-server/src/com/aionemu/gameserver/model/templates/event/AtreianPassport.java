package com.aionemu.gameserver.model.templates.event;

import java.time.LocalDateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.aionemu.gameserver.dataholders.loadingutils.adapters.LocalDateTimeAdapter;
import com.aionemu.gameserver.model.AttendType;

/**
 * @author Alcapwnd
 */
@XmlRootElement(name = "login_event")
@XmlAccessorType(XmlAccessType.NONE)
public class AtreianPassport {

	@XmlAttribute(name = "id", required = true)
	private int id;
	@XmlAttribute(name = "active", required = true)
	private boolean active;
	@XmlAttribute(name = "period_start", required = true)
	@XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
	private LocalDateTime pStart;
	@XmlAttribute(name = "period_end", required = true)
	@XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
	private LocalDateTime pEnd;
	@XmlAttribute(name = "attend_type", required = true)
	private AttendType attendType;
	@XmlAttribute(name = "attend_num")
	private int attendNum;
	@XmlAttribute(name = "reward_item", required = true)
	private int rewardItemId;
	@XmlAttribute(name = "reward_item_num", required = true)
	private int rewardItemCount;

	public int getId() {
		return id;
	}

	public boolean isActive() {
		return active;
	}

	public LocalDateTime getPeriodStart() {
		return pStart;
	}

	public LocalDateTime getPeriodEnd() {
		return pEnd;
	}

	public AttendType getAttendType() {
		return attendType;
	}

	public int getAttendNum() {
		return attendNum;
	}

	public int getRewardItemId() {
		return rewardItemId;
	}

	public int getRewardItemCount() {
		return rewardItemCount;
	}
}
