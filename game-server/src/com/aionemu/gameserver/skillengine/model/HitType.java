package com.aionemu.gameserver.skillengine.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Sippolo
 */
@XmlType(name = "HitType")
@XmlEnum
public enum HitType {
	EVERYHIT,
	NMLATK,
	MAHIT,
	PHHIT,
	FEAR,
	SKILL,
	BACKATK
}
