package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author AionCool
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropNpcs")
public class GlobalDropNpcs {

	@XmlElement(name = "gd_npc")
	protected List<GlobalDropNpc> gdNpcs;

	public List<GlobalDropNpc> getGlobalDropNpcs() {
		if (gdNpcs == null) {
			gdNpcs = new ArrayList<>();
		}
		return this.gdNpcs;
	}

	public void addNpcs(List<GlobalDropNpc> value) {
		this.gdNpcs = value;
	}

}
