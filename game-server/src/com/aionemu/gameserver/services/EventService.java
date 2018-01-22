package com.aionemu.gameserver.services;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EventTheme;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.Guides.GuideTemplate;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.event.InventoryDrop;
import com.aionemu.gameserver.model.templates.globaldrops.GlobalRule;
import com.aionemu.gameserver.model.templates.quest.QuestCategory;
import com.aionemu.gameserver.model.templates.spawns.Spawn;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnMap;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION.ActionType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_VERSION_CHECK;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.time.ServerTime;
import com.aionemu.gameserver.world.World;

/**
 * @author Rolandas
 * @reworked Neon
 */
public class EventService {

	private static final Logger log = LoggerFactory.getLogger(EventService.class);

	private volatile JobDetail checkTask = null;
	private volatile Set<Event> activeEvents = Collections.emptySet();
	private volatile List<GlobalRule> activeEventDropRules = Collections.emptyList();
	private volatile Set<Integer> activeEventQuests = Collections.emptySet();
	private volatile EventTheme eventTheme = EventTheme.NONE;

	private static class SingletonHolder {

		protected static final EventService instance = new EventService();
	}

	public static final EventService getInstance() {
		return SingletonHolder.instance;
	}

	private EventService() {
		start();
	}

	public boolean start() {
		if (checkTask != null)
			return false;

		validateConfiguredEventNames();
		checkEvents();
		checkTask = CronService.getInstance().schedule(() -> checkEvents(), "0 0/5 * ? * *");
		return true;
	}

	public void stop() {
		if (checkTask != null) {
			CronService.getInstance().cancel(checkTask);
			checkTask = null;
			for (Event event : activeEvents)
				event.stop();
			activeEvents = Collections.emptySet();
			activeEventQuests = Collections.emptySet();
			activeEventDropRules = Collections.emptyList();
			updateEventType();
		}
	}

	private void validateConfiguredEventNames() {
		if (!isAllEvents(EventsConfig.ENABLED_EVENTS)) {
			Set<String> eventNames = DataManager.EVENT_DATA.getEvents().stream().map(EventTemplate::getName).collect(Collectors.toSet());
			EventsConfig.ENABLED_EVENTS.forEach(eventName -> {
				if (!eventNames.contains(eventName))
					log.warn("Unknown event \"" + eventName + "\" configured");
			});
		}
	}

	public void onPlayerLogin(Player player) {
		activeEvents.forEach(event -> event.onPlayerLogin(player));
	}

	private boolean isAllEvents(List<String> list) {
		return list.size() == 1 && "*".equals(list.get(0));
	}

	private void checkEvents() {
		Set<Event> newActiveEvents = collectActiveEvents();
		if (!activeEvents.equals(newActiveEvents)) {
			startOrStopEvents(activeEvents, newActiveEvents);
			activeEventQuests = collectQuestIds(newActiveEvents);
			activeEventDropRules = collectDropRules(newActiveEvents);
			activeEvents = newActiveEvents;
			updateEventType();
		}
	}

	private Set<Event> collectActiveEvents() {
		List<EventTemplate> enabledEvents = isAllEvents(EventsConfig.ENABLED_EVENTS) ? DataManager.EVENT_DATA.getEvents()
			: DataManager.EVENT_DATA.getEvents(EventsConfig.ENABLED_EVENTS);
		if (enabledEvents.isEmpty())
			return Collections.emptySet();
		Set<Event> newActiveEvents = new HashSet<>();
		LocalDateTime now = ServerTime.now().toLocalDateTime();
		for (EventTemplate et : enabledEvents) {
			if (et.isInEventPeriod(now))
				newActiveEvents.add(findOrCreateEvent(et));
		}
		return newActiveEvents;
	}

	private Event findOrCreateEvent(EventTemplate et) {
		return activeEvents.stream().filter(event -> et.equals(event.getEventTemplate())).findFirst().orElseGet(() -> new Event(et));
	}

	private void startOrStopEvents(Set<Event> oldActiveEvents, Set<Event> newActiveEvents) {
		for (Event oldActiveEvent : oldActiveEvents) {
			if (!newActiveEvents.contains(oldActiveEvent))
				oldActiveEvent.stop();
		}
		for (Event newActiveEvent : newActiveEvents) {
			if (!oldActiveEvents.contains(newActiveEvent))
				newActiveEvent.start();
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
		return events.stream().filter(t -> t.getEventTemplate().getEventDrops() != null)
			.flatMap(t -> t.getEventTemplate().getEventDrops().getAllRules().stream()).collect(Collectors.toList());
	}

	public boolean isActiveEventQuest(int questId) {
		return activeEventQuests.contains(questId);
	}

	private void updateEventType() {
		EventTheme oldEventTheme = eventTheme;
		EventTheme newEventTheme = null;
		for (Event event : activeEvents) {
			if (event.getEventTemplate().getTheme() != null) {
				newEventTheme = event.getEventTemplate().getTheme();
				break;
			}
		}
		eventTheme = newEventTheme == null ? EventTheme.NONE : newEventTheme;
		if (oldEventTheme != eventTheme) // update city decoration (logged in players see changes after teleport)
			PacketSendUtility.broadcastToWorld(new SM_VERSION_CHECK(eventTheme));
	}

	public EventTheme getEventTheme() {
		return eventTheme;
	}

	public List<GlobalRule> getActiveEventDropRules() {
		return activeEventDropRules;
	}

	private static class Event {

		private final EventTemplate eventTemplate;
		private Future<?> inventoryDropTask;

		public Event(EventTemplate eventTemplate) {
			this.eventTemplate = eventTemplate;
		}

		public EventTemplate getEventTemplate() {
			return eventTemplate;
		}

		public void start() {
			if (eventTemplate.getSpawns() != null && eventTemplate.getSpawns().size() > 0) { // TODO refactor SpawnEngine to use its methods
				for (SpawnMap map : eventTemplate.getSpawns().getTemplates()) {
					DataManager.SPAWNS_DATA.addNewSpawnMap(map);
					Collection<Integer> instanceIds = World.getInstance().getWorldMap(map.getMapId()).getAvailableInstanceIds();
					for (Integer instanceId : instanceIds) {
						int spawnCount = 0;
						for (Spawn spawn : map.getSpawns()) {
							spawn.setEventTemplate(eventTemplate);
							SpawnGroup group = new SpawnGroup(map.getMapId(), spawn);
							if (group.hasPool() && SpawnEngine.checkPool(group)) {
								group.resetTemplates(instanceId);
								for (int i = 0; i < group.getPool(); i++) {
									SpawnTemplate t = group.getRndTemplate(instanceId);
									if (t == null)
										break;
									SpawnEngine.spawnObject(t, instanceId);
									spawnCount++;
								}
							} else {
								for (SpawnTemplate t : group.getSpawnTemplates()) {
									SpawnEngine.spawnObject(t, instanceId);
									spawnCount++;
								}
							}
						}
						log.info("Spawned event objects in " + map.getMapId() + " [" + instanceId + "]: " + spawnCount + " (" + eventTemplate.getName() + ")");
					}
				}
				DataManager.SPAWNS_DATA.afterUnmarshal(null, null);
				DataManager.SPAWNS_DATA.clearTemplates();
			}

			if (inventoryDropTask != null) {
				InventoryDrop inventoryDrop = eventTemplate.getInventoryDrop();
				inventoryDropTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
					World.getInstance().forEachPlayer(player -> {
						if (player.getLevel() >= inventoryDrop.getStartLevel())
							// TODO: check the exact type in retail
							ItemService.addItem(player, inventoryDrop.getDropItem(), inventoryDrop.getCount(), true,
								new ItemUpdatePredicate(ItemAddType.ITEM_COLLECT, ItemUpdateType.INC_CASH_ITEM));
					});
				}, 0, inventoryDrop.getInterval() * 60000);
			}

			if (eventTemplate.getSurveys() != null) {
				for (String survey : eventTemplate.getSurveys()) {
					GuideTemplate template = DataManager.GUIDE_HTML_DATA.getTemplateByTitle(survey);
					if (template != null)
						template.setActivated(true);
				}
			}

			log.info("Started event: " + eventTemplate.getName());
		}

		public void stop() {
			if (eventTemplate.getSpawns() != null && eventTemplate.getSpawns().size() > 0) {
				int[] count = { 0 };
				World.getInstance().forEachObject(o -> {
					SpawnTemplate spawn = o.getSpawn();
					if (spawn != null && eventTemplate.equals(spawn.getEventTemplate())) {
						o.getController().delete();
						count[0]++;
					}
				});
				count[0] += RespawnService.cancelEventRespawns(eventTemplate);
				DataManager.SPAWNS_DATA.removeEventSpawnObjects(eventTemplate);
				log.info("Removed " + count[0] + " event spawns (" + eventTemplate.getName() + ")");
			}

			if (inventoryDropTask != null) {
				inventoryDropTask.cancel(false);
				inventoryDropTask = null;
			}

			if (eventTemplate.getSurveys() != null) {
				for (String survey : eventTemplate.getSurveys()) {
					GuideTemplate template = DataManager.GUIDE_HTML_DATA.getTemplateByTitle(survey);
					if (template != null)
						template.setActivated(false);
				}
			}

			log.info("Stopped event: " + eventTemplate.getName());
		}

		public void onPlayerLogin(Player player) {
			startOrMaintainQuests(player);
		}

		private void startOrMaintainQuests(Player player) {
			for (int startableQuestId : eventTemplate.getStartableQuests()) {
				if (isAllowedToStartEventQuest(player, startableQuestId)) {
					QuestState qs = player.getQuestStateList().getQuestState(startableQuestId);
					if (qs == null) {
						qs = new QuestState(startableQuestId, QuestStatus.START);
						player.getQuestStateList().addQuest(startableQuestId, qs);
						PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(ActionType.ADD, qs));
					} else if (qs.getStatus() != QuestStatus.START && qs.getCompleteCount() > 0) {
						LocalDateTime completeTime = ServerTime.atDate(qs.getLastCompleteTime()).toLocalDateTime();
						if (eventTemplate.getStartDate().isAfter(completeTime)) { // quest was last completed on a previous event, reset & restart it
							ActionType actionType = qs.getStatus() == QuestStatus.COMPLETE ? ActionType.ADD : ActionType.UPDATE;
							qs.setStatus(QuestStatus.START);
							qs.setQuestVar(0);
							qs.setCompleteCount(0);
							qs.setRewardGroup(null);
							PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(actionType, qs));
						}
					}
				}
			}
			for (int maintainableQuestId : eventTemplate.getMaintainableQuests()) {
				QuestState qs = player.getQuestStateList().getQuestState(maintainableQuestId);
				if (qs != null && qs.getCompleteCount() > 0 && isAllowedToStartEventQuest(player, maintainableQuestId)) {
					LocalDateTime completeTime = ServerTime.atDate(qs.getLastCompleteTime()).toLocalDateTime();
					if (eventTemplate.getStartDate().isAfter(completeTime)) { // quest was last completed on a previous event, reset it
						qs.setCompleteCount(0);
					}
				}
			}
		}

		private boolean isAllowedToStartEventQuest(Player player, int questId) {
			QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
			if (template.getCategory() != QuestCategory.EVENT)
				return false;
			if (!QuestService.checkStartConditions(player, questId, false, 0, true, true, false))
				return false;
			return true;
		}
	}
}
