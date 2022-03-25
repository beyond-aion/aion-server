package com.aionemu.gameserver.services.event;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dao.EventDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EventTheme;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalRule;
import com.aionemu.gameserver.network.aion.serverpackets.SM_VERSION_CHECK;
import com.aionemu.gameserver.skillengine.model.Effect.ForceType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author Rolandas, Neon
 */
public class EventService {

	private static final Logger log = LoggerFactory.getLogger(EventService.class);

	private volatile JobDetail checkTask = null;
	private volatile Set<Event> activeEvents = Collections.emptySet();
	private volatile List<GlobalRule> activeEventDropRules = Collections.emptyList();
	private volatile Set<Integer> activeEventQuests = Collections.emptySet();
	private volatile Set<ForceType> effectForceTypes = Collections.emptySet();
	private volatile EventTheme eventTheme = EventTheme.NONE;

	private EventService() {
		start();
	}

	public boolean start() {
		if (checkTask != null)
			return false;

		validateConfiguredEventNames();
		checkActiveEvents();
		checkTask = CronService.getInstance().schedule(this::onTimeChanged, "0 0/5 * ? * *");
		return true;
	}

	public void stop() {
		if (checkTask != null) {
			CronService.getInstance().cancel(checkTask);
			checkTask = null;
			Set<Event> oldActiveEvents = activeEvents;
			activeEvents = Collections.emptySet();
			activeEventQuests = Collections.emptySet();
			activeEventDropRules = Collections.emptyList();
			updateEventTheme();
			for (Event event : oldActiveEvents) // iterate after emptying activeEvents to ensure correct handling in stop()
				event.stop();
		}
	}

	private void validateConfiguredEventNames() {
		if (!isAllEvents(EventsConfig.DISABLED_EVENTS)) {
			Set<String> eventNames = DataManager.EVENT_DATA.getEvents().stream().map(EventTemplate::getName).collect(Collectors.toSet());
			EventsConfig.DISABLED_EVENTS.forEach(eventName -> {
				if (!eventNames.contains(eventName))
					log.warn("Unknown event \"" + eventName + "\" configured as disabled");
			});
		}
	}

	private void checkActiveEvents() {
		Set<Event> oldActiveEvents = activeEvents;
		Set<Event> newActiveEvents = collectActiveEvents();
		if (!oldActiveEvents.equals(newActiveEvents)) {
			activeEvents = newActiveEvents;
			activeEventQuests = collectQuestIds(activeEvents);
			activeEventDropRules = collectDropRules(activeEvents);
			updateEventTheme();
			startOrStopEvents(oldActiveEvents, activeEvents);
			effectForceTypes = activeEvents.stream().map(Event::getEffectForceType).filter(Objects::nonNull).collect(Collectors.toSet());
		}
	}

	public boolean isInactiveEventForceType(ForceType forceType) {
		return Event.isEventEffectForceType(forceType) && !effectForceTypes.contains(forceType);
	}

	private void onTimeChanged() {
		checkActiveEvents();
		if (!activeEvents.isEmpty()) {
			ZonedDateTime now = ServerTime.now();
			activeEvents.forEach(event -> event.onTimeChanged(now));
		}
	}

	public void onPlayerLogin(Player player) {
		activeEvents.forEach(event -> event.onPlayerLogin(player));
	}

	public void onEnteredTeam(Player player, TemporaryPlayerTeam<? extends TeamMember<Player>> team) {
		activeEvents.forEach(event -> event.onEnteredTeam(player, team));
	}

	public void onLeftTeam(Player player, TemporaryPlayerTeam<? extends TeamMember<Player>> team) {
		activeEvents.forEach(event -> event.onLeftTeam(player, team));
	}

	public void onEnterMap(Player player) {
		activeEvents.forEach(event -> event.onEnterMap(player));
	}

	public void onPveKill(Player killer, Npc victim) {
		activeEvents.forEach(event -> event.onPveKill(killer, victim));
	}

	public void onPvpKill(Player killer, Player victim) {
		activeEvents.forEach(event -> event.onPvpKill(killer, victim));
	}

	private boolean isAllEvents(Set<String> list) {
		return list.size() == 1 && "*".equals(list.iterator().next());
	}

	private Set<Event> collectActiveEvents() {
		if (isAllEvents(EventsConfig.DISABLED_EVENTS))
			return Collections.emptySet();
		LocalDateTime now = ServerTime.now().toLocalDateTime();
		return DataManager.EVENT_DATA.getEvents().stream()
				.filter(et -> !EventsConfig.DISABLED_EVENTS.contains(et.getName()) && et.isInEventPeriod(now))
				.map(this::findOrCreateEvent)
				.collect(Collectors.toSet());
	}

	private Event findOrCreateEvent(EventTemplate et) {
		return activeEvents.stream().filter(event -> et.equals(event.getEventTemplate())).findFirst().orElseGet(() -> new Event(et));
	}

	private void startOrStopEvents(Set<Event> oldActiveEvents, Set<Event> newActiveEvents) {
		for (Event oldActiveEvent : oldActiveEvents) {
			if (!newActiveEvents.contains(oldActiveEvent))
				oldActiveEvent.stop();
		}
		if (!newActiveEvents.isEmpty()) {
			boolean cleanedOldBuffData = false;
			for (Event newActiveEvent : newActiveEvents) {
				if (!oldActiveEvents.contains(newActiveEvent)) {
					if (!cleanedOldBuffData) {
						cleanedOldBuffData = true;
						DAOManager.getDAO(EventDAO.class).deleteOldBuffData();
					}
					newActiveEvent.start();
				}
			}
		}
	}

	private Set<Integer> collectQuestIds(Set<Event> events) {
		Set<Integer> questIds = new HashSet<>();
		for (Event event : events) {
			questIds.addAll(event.getEventTemplate().getStartableQuests());
			questIds.addAll(event.getEventTemplate().getMaintainableQuests());
		}
		return questIds;
	}

	private List<GlobalRule> collectDropRules(Set<Event> events) {
		return events.stream().filter(e -> e.getEventTemplate().getEventDropRules() != null)
			.flatMap(e -> e.getEventTemplate().getEventDropRules().stream()).collect(Collectors.toList());
	}

	public Set<Event> getActiveEvents() {
		return activeEvents;
	}

	public boolean isEventActive(String eventName) {
		return activeEvents.stream().anyMatch(e -> e.getEventTemplate().getName().equals(eventName));
	}

	public boolean isActiveEventQuest(int questId) {
		return activeEventQuests.contains(questId);
	}

	private void updateEventTheme() {
		EventTheme oldEventTheme = eventTheme;
		EventTheme newEventTheme = EventTheme.NONE;
		for (Event event : activeEvents) {
			if (event.getEventTemplate().getTheme() != null) {
				newEventTheme = event.getEventTemplate().getTheme();
				break;
			}
		}
		if (oldEventTheme != newEventTheme) {
			eventTheme = newEventTheme;
			PacketSendUtility.broadcastToWorld(new SM_VERSION_CHECK(eventTheme)); // update city decoration (logged in players see changes after teleport)
		}
	}

	public EventTheme getEventTheme() {
		return eventTheme;
	}

	public List<GlobalRule> getActiveEventDropRules() {
		return activeEventDropRules;
	}

	private static class SingletonHolder {

		protected static final EventService instance = new EventService();
	}

	public static EventService getInstance() {
		return SingletonHolder.instance;
	}
}
