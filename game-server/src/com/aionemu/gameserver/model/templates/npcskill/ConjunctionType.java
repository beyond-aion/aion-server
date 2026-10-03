package com.aionemu.gameserver.model.templates.npcskill;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author nrg
 */
@XmlType(name = "ConjunctionType")
@XmlEnum
public enum ConjunctionType {

	AND,
	OR,
	XOR;

	public String value() {
		return name();
	}

	public static ConjunctionType fromValue(String v) {
		return valueOf(v);
	}

}
