package com.aionemu.gameserver.model.templates.factions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author vlog
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FactionCategory")
public enum FactionCategory {

	MENTOR,
	DAILY,
	COMBINESKILL,
	SHUGO;
}
