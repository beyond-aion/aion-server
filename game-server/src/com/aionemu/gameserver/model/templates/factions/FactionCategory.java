package com.aionemu.gameserver.model.templates.factions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

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
