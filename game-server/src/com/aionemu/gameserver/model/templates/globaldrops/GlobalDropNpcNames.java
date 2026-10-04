package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author bobobear
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropNpcNames")
public class GlobalDropNpcNames {

	@XmlElement(name = "gd_npc_name")
	protected List<GlobalDropNpcName> gdNpcNames;

	public List<GlobalDropNpcName> getGlobalDropNpcNames() {
		if (gdNpcNames == null) {
			gdNpcNames = new ArrayList<>();
		}
		return this.gdNpcNames;
	}

}
