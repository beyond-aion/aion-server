package com.aionemu.gameserver.dataholders;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.event.EventTemplate;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventData")
@XmlRootElement(name = "timed_events")
public class EventData {

	@XmlElement(name = "event")
	private List<EventTemplate> events;

	@XmlTransient
	private Set<Integer> allNpcIds = new HashSet<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (events == null)
			events = Collections.emptyList();
		allNpcIds.clear();
		for (EventTemplate ev : events) {
			if (ev.getSpawns() != null)
				allNpcIds.addAll(ev.getSpawns().getAllNpcIds());
		}
	}

	public int size() {
		return events.size();
	}

	public List<EventTemplate> getEvents() {
		return events;
	}

	public void setEvents(List<EventTemplate> events) {
		this.events = events;
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
