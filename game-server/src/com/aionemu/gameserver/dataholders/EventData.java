package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.event.EventTemplate;

import javolution.util.FastMap;
import javolution.util.FastTable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventData")
@XmlRootElement(name = "timed_events")
public class EventData {

	@XmlElement(name = "event")
	protected List<EventTemplate> events;

	@XmlTransient
	private FastMap<String, EventTemplate> allEvents = new FastMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (events == null)
			return;

		allEvents.clear();
		for (EventTemplate ev : events)
			allEvents.put(ev.getName(), ev);

		events.clear();
		events = null;
	}

	public int size() {
		return allEvents.size();
	}

	public List<String> getEventNames() {
		List<String> result = new FastTable<>();
		synchronized (allEvents) {
			result.addAll(allEvents.keySet());
		}

		return result;
	}

	public List<EventTemplate> getEvents() {
		List<EventTemplate> result = new FastTable<>();
		synchronized (allEvents) {
			result.addAll(allEvents.values());
		}

		return result;
	}

	public EventTemplate getEvent(String name) {
		return allEvents.get(name);
	}

	public void setEvents(List<EventTemplate> events) {
		for (EventTemplate et : allEvents.values())
			et.stop();

		this.events = events == null ? new FastTable<>() : events;
		afterUnmarshal(null, null);
	}
}
