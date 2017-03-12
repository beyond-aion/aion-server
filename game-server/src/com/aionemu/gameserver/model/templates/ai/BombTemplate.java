package com.aionemu.gameserver.model.templates.ai;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BombTemplate")
public class BombTemplate {

	@XmlAttribute(name = "skillId")
	private int skillId = 0;
	@XmlAttribute(name = "cd")
	private int cd = 0;

	public int getCd() {
		return cd;
	}

	public int getSkillId() {
		return skillId;
	}
}
