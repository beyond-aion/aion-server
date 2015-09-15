package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastTable;

/**
 * @author Bobobear
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropExcludedNpcs")
public class GlobalDropExcludedNpcs {

	@XmlElement(name = "gd_excluded_npc")
	protected List<GlobalDropExcludedNpc> gdExcludedNpcs;

	public List<GlobalDropExcludedNpc> getGlobalDropExcludedNpcs() {
		if (gdExcludedNpcs == null) {
			gdExcludedNpcs = new FastTable<GlobalDropExcludedNpc>();
		}
		return this.gdExcludedNpcs;
	}

}
