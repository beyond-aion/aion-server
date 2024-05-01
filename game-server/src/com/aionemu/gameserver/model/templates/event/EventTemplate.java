package com.aionemu.gameserver.model.templates.event;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.aionemu.gameserver.dataholders.SpawnsData;
import com.aionemu.gameserver.dataholders.loadingutils.adapters.LocalDateTimeAdapter;
import com.aionemu.gameserver.model.EventTheme;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalRule;

/**
 * @author Rolandas, Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventTemplate")
public class EventTemplate {

	@XmlAttribute(name = "name", required = true)
	private String name;

	@XmlAttribute(name = "start")
	@XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
	private LocalDateTime startDate;

	@XmlAttribute(name = "end")
	@XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
	private LocalDateTime endDate;

	@XmlAttribute(name = "theme")
	private EventTheme theme;

	@XmlElement(name = "login_message")
	private String loginMessage;

	@XmlElementWrapper(name = "config_properties")
	@XmlElement(name = "property")
	private List<String> configProperties;

	@XmlElementWrapper(name = "event_drops")
	@XmlElement(name = "gd_rule")
	private List<GlobalRule> eventDropRules;

	@XmlElement(name = "quests")
	private EventQuestList quests;

	@XmlElement(name = "spawns")
	private SpawnsData spawns;

	@XmlElement(name = "inventory_drop")
	private InventoryDrop inventoryDrop;

	@XmlList
	@XmlElement(name = "surveys")
	private List<String> surveys;

	@XmlElementWrapper(name = "buffs")
	@XmlElement(name = "buff")
	private List<Buff> buffs;

	public String getName() {
		return name;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public EventTheme getTheme() {
		return theme;
	}

	public String getLoginMessage() {
		return loginMessage;
	}

	public boolean hasConfigProperties() {
		return configProperties != null;
	}

	public Properties loadConfigProperties() throws IOException {
		Properties configProperties = new Properties();
		configProperties.load(new StringReader(String.join("\n", this.configProperties)));
		return configProperties;
	}

	public List<GlobalRule> getEventDropRules() {
		return eventDropRules;
	}

	public SpawnsData getSpawns() {
		return spawns;
	}

	public InventoryDrop getInventoryDrop() {
		return inventoryDrop;
	}

	public List<Integer> getStartableQuests() {
		return quests == null ? Collections.emptyList() : quests.getStartableQuests();
	}

	public List<Integer> getMaintainableQuests() {
		return quests == null ? Collections.emptyList() : quests.getMaintainQuests();
	}

	public boolean isInEventPeriod(LocalDateTime time) {
		return (startDate == null || !time.isBefore(startDate)) && (endDate == null || time.isBefore(endDate));
	}

	public List<String> getSurveys() {
		return surveys;
	}

	public List<Buff> getBuffs() {
		return buffs;
	}

}
