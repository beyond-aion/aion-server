package com.aionemu.gameserver.model.ai;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BombTemplate")
public class BombTemplate {
	@XmlAttribute(name = "skillId")
	private int SkillId = 0;
	@XmlAttribute(name = "cd")
	private int cd = 0;

	public int getCd() {
		return this.cd;
	}

	public int getSkillId() {
		return this.SkillId;
	}
}
