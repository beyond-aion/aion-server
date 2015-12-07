package com.aionemu.gameserver.dataholders;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastMap;
import javolution.util.FastTable;

import com.aionemu.gameserver.model.templates.event.EventTemplate;

/**
 * <p>
 * Java class for EventData complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EventData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="event" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;extension base="{}EventTemplate">
 *               &lt;/extension>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventData")
@XmlRootElement(name = "events_config")
public class EventData {

	@XmlElementWrapper(name = "events")
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
			et.Stop();

		this.events = events == null ? new FastTable<>() : events;
		afterUnmarshal(null, null);
	}
}
