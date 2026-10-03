package com.aionemu.gameserver.model.templates.ai;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Bombs")
public class Bombs {

	@XmlElement(name = "bomb")
	private BombTemplate bombTemplate;

	public BombTemplate getBombTemplate() {
		return bombTemplate;
	}
}
