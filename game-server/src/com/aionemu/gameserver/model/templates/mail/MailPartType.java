package com.aionemu.gameserver.model.templates.mail;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType(name = "MailPartType")
@XmlEnum
public enum MailPartType {

	CUSTOM,
	SENDER,
	TITLE,
	HEADER,
	BODY,
	TAIL;

	public String value() {
		return name();
	}

	public static MailPartType fromValue(String v) {
		return valueOf(v);
	}

}
