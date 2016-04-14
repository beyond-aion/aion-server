package com.aionemu.gameserver.model.templates.npcskill;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

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
