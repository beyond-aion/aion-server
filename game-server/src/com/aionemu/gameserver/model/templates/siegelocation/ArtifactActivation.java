package com.aionemu.gameserver.model.templates.siegelocation;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Wakizashi
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArtifactActivation")
public class ArtifactActivation {

	@XmlAttribute(name = "item_id")
	protected int itemId;
	@XmlAttribute(name = "count")
	protected int count;
	@XmlAttribute(name = "skill")
	protected int skill;
	@XmlAttribute(name = "cd")
	protected int cd;

	@XmlAttribute(name = "repeat_count")
	protected int repeatCount = 1;
	@XmlAttribute(name = "repeat_interval")
	protected int repeatInterval = 1;

	public int getItemId() {
		return itemId;
	}

	public int getCount() {
		return count;
	}

	public int getSkillId() {
		return skill;
	}

	public long getCd() {
		return cd * 1000;
	}

	public int getRepeatCount() {
		return repeatCount;
	}

	public int getRepeatInterval() {
		return repeatInterval;
	}
}
