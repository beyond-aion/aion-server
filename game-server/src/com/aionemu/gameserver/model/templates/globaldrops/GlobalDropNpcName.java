package com.aionemu.gameserver.model.templates.globaldrops;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Bobobear
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropNpcName")
public class GlobalDropNpcName {

	@XmlAttribute(name = "value", required = true)
	protected String name;

	@XmlAttribute(name = "function", required = true)
	protected StringFunction function;

	public String getValue() {
		return name;
	}

	public StringFunction getFunction() {
		return function;
	}
}
