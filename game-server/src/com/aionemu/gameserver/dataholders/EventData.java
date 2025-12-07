package com.aionemu.gameserver.dataholders;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (events == null)
			events = Collections.emptyList();
		for (EventTemplate ev : events) {
			if (ev.getEndDate() != null && ev.getStartDate() != null && !ev.getStartDate().isBefore(ev.getEndDate()))
				throw new IllegalArgumentException("Event \"" + ev.getName() + "\" has an invalid start or end date: start date must be before end date");
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

	public void addAllNpcIdsToSet(Set<Integer> npcIds) {
		events.stream().map(EventTemplate::getSpawns).filter(Objects::nonNull).forEach(spawns -> spawns.addAllNpcIdsToSet(npcIds));
	}
}
