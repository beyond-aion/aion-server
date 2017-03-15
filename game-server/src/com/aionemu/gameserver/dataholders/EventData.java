package com.aionemu.gameserver.dataholders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.event.EventTemplate;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventData")
@XmlRootElement(name = "timed_events")
public class EventData {

	@XmlElement(name = "event")
	protected List<EventTemplate> events;

	@XmlTransient
	private Map<String, EventTemplate> allEvents = new HashMap<>();

	@XmlTransient
	private Set<Integer> allNpcIds = new HashSet<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (events == null)
			return;

		allEvents.clear();
		allNpcIds.clear();
		for (EventTemplate ev : events) {
			allEvents.put(ev.getName(), ev);
			if (ev.getSpawns() != null)
				allNpcIds.addAll(ev.getSpawns().getAllNpcIds());
		}

		events.clear();
		events = null;
	}

	public int size() {
		return allEvents.size();
	}

	public List<String> getEventNames() {
		return new ArrayList<>(allEvents.keySet());
	}

	public List<EventTemplate> getEvents() {
		return new ArrayList<>(allEvents.values());
	}

	public EventTemplate getEvent(String name) {
		return allEvents.get(name);
	}

	public void setEvents(List<EventTemplate> events) {
		for (EventTemplate et : allEvents.values())
			et.stop();

		this.events = events == null ? Collections.emptyList() : events;
		afterUnmarshal(null, null);
	}

	/**
	 * @param npcId
	 * @return True, if the given npc appears in any of the spawn templates (town level 1-5)
	 */
	public boolean containsAnySpawnForNpc(int npcId) {
		return allNpcIds.contains(npcId);
	}
}
