package com.aionemu.gameserver.model.templates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.instance.InstanceCoolTimeType;

/**
 * @author VladimirZ
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InstanceCooltime")
public class InstanceCooltime {

	@XmlElement(name = "type")
	protected InstanceCoolTimeType coolTimeType;
	@XmlElement(name = "typevalue")
	protected String typevalue;
	@XmlElement(name = "ent_cool_time")
	protected int entCoolTime;
	@XmlElement(name = "maxcount")
	protected int maxCount;
	@XmlElement(name = "max_member_light")
	protected int maxMemberLight;
	@XmlElement(name = "max_member_dark")
	protected int maxMemberDark;
	@XmlElement(name = "enter_min_level_light")
	protected int enterMinLevelLight;
	@XmlElement(name = "enter_max_level_light")
	protected int enterMaxLevelLight;
	@XmlElement(name = "enter_min_level_dark")
	protected int enterMinLevelDark;
	@XmlElement(name = "enter_max_level_dark")
	protected int enterMaxLevelDark;
	@XmlElement(name = "can_enter_mentor")
	protected boolean can_enter_mentor;
	@XmlAttribute(required = true)
	protected int id;
	@XmlAttribute(required = true)
	protected int worldId;
	@XmlAttribute(required = true)
	protected Race race;
	@XmlAttribute(name = "sync_id")
	private int syncId;

	public InstanceCoolTimeType getCoolTimeType() {
		return coolTimeType;
	}

	public String getTypeValue() {
		return typevalue;
	}

	public int getEntCoolTime() {
		return entCoolTime;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public int getMaxMemberLight() {
		return maxMemberLight;
	}

	public int getMaxMemberDark() {
		return maxMemberDark;
	}

	public int getEnterMinLevelLight() {
		return enterMinLevelLight;
	}

	public int getEnterMaxLevelLight() {
		return enterMaxLevelLight;
	}

	public int getEnterMinLevelDark() {
		return enterMinLevelDark;
	}

	public int getEnterMaxLevelDark() {
		return enterMaxLevelDark;
	}

	public boolean getCanEnterMentor() {
		return can_enter_mentor;
	}

	public int getId() {
		return id;
	}

	public int getWorldId() {
		return worldId;
	}

	public Race getRace() {
		return race;
	}

	public int getSyncId() {
		return syncId;
	}
}
