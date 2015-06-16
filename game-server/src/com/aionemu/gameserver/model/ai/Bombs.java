package com.aionemu.gameserver.model.ai;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Bombs")
public class Bombs {

	@XmlElement(name = "bomb")
	private BombTemplate bombTemplate;

	public BombTemplate getBombTemplate() {
		return this.bombTemplate;
	}
}