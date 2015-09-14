package com.aionemu.gameserver.model.templates.challenge;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ChallengeType")
@XmlEnum
public enum ChallengeType {
	LEGION(1),
	TOWN(2);

	private int id;

	public int getId() {
		return this.id;
	}

	private ChallengeType(int id) {
		this.id = id;
	}

	public String value() {
		return name();
	}

	public static ChallengeType fromValue(String paramString) {
		return valueOf(paramString);
	}
}
