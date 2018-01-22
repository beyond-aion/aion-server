package com.aionemu.gameserver.model.templates.event;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.aionemu.gameserver.dataholders.SpawnsData;
import com.aionemu.gameserver.dataholders.loadingutils.adapters.LocalDateTimeAdapter;
import com.aionemu.gameserver.model.EventTheme;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventTemplate")
public class EventTemplate {

	@XmlElement(name = "event_drops", required = false)
	private EventDrops eventDrops;

	@XmlElement(name = "quests", required = false)
	private EventQuestList quests;

	@XmlElement(name = "spawns", required = false)
	private SpawnsData spawns;

	@XmlElement(name = "inventory_drop", required = false)
	private InventoryDrop inventoryDrop;

	@XmlList
	@XmlElement(name = "surveys", required = false)
	private List<String> surveys;

	@XmlAttribute(name = "name", required = true)
	private String name;

	@XmlAttribute(name = "start", required = true)
	@XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
	private LocalDateTime startDate;

	@XmlAttribute(name = "end", required = true)
	@XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
	private LocalDateTime endDate;

	@XmlAttribute(name = "theme", required = false)
	private EventTheme theme;

	public String getName() {
		return name;
	}

	public EventDrops getEventDrops() {
		return eventDrops;
	}

	public SpawnsData getSpawns() {
		return spawns;
	}

	public InventoryDrop getInventoryDrop() {
		return inventoryDrop;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public List<Integer> getStartableQuests() {
		return quests == null ? Collections.emptyList() : quests.getStartableQuests();
	}

	public List<Integer> getMaintainableQuests() {
		return quests == null ? Collections.emptyList() : quests.getMaintainQuests();
	}

	public boolean isInEventPeriod(LocalDateTime time) {
		return !time.isBefore(getStartDate()) && time.isBefore(getEndDate());
	}

	public List<String> getSurveys() {
		return surveys;
	}

	public EventTheme getTheme() {
		return theme;
	}

}
