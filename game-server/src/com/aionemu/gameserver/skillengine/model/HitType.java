package com.aionemu.gameserver.skillengine.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

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
