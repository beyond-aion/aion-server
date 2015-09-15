package com.aionemu.gameserver.model.templates.event;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastTable;

import com.aionemu.gameserver.model.templates.globaldrops.GlobalRule;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventDrops")
public class EventDrops {

	@XmlElement(name = "gd_rule")
	protected List<GlobalRule> globalDropRules;

	/**
	 * Gets the value of the globalDrop property.
	 */
	public List<GlobalRule> getAllRules() {
		if (globalDropRules == null) {
			globalDropRules = new FastTable<GlobalRule>();
		}
		return this.globalDropRules;
	}

	public int size() {
		return globalDropRules.size();
	}
}
