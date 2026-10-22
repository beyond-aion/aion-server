package com.aionemu.gameserver.model.templates.event;

import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlList;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlType(name = "EventQuestList")
@XmlAccessorType(XmlAccessType.FIELD)
public class EventQuestList {

	@XmlElement(name = "startable")
	@XmlList
	private List<Integer> startQuests;

	@XmlElement(name = "maintainable")
	@XmlList
	private List<Integer> maintainQuests;

	/**
	 * @return the startQuests (automatically started on logon)
	 */
	public List<Integer> getStartableQuests() {
		if (startQuests == null)
			return Collections.emptyList();
		return startQuests;
	}

	/**
	 * @return the maintainQuests (started indirectly from other quests)
	 */
	public List<Integer> getMaintainQuests() {
		if (maintainQuests == null)
			return Collections.emptyList();
		return maintainQuests;
	}

}
