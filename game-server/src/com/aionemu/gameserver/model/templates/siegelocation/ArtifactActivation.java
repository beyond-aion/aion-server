package com.aionemu.gameserver.model.templates.siegelocation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

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
}
