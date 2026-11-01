package com.aionemu.gameserver.model.templates.item;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Rank of a stigma stone, taken from the item templates. Higher ranks fit fewer slots.
 */
@XmlType(name = "stigmaType")
@XmlEnum
public enum StigmaType {

	@XmlEnumValue("normal")
	NORMAL,
	@XmlEnumValue("greater")
	GREATER,
	@XmlEnumValue("major")
	MAJOR
}
