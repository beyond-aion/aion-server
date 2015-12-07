package com.aionemu.gameserver.services;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Future;

import javolution.util.FastTable;

import org.joda.time.DateTime;

import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EventType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.quest.XMLStartCondition;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rolandas
 * @modified Neon
 */
public class EventService {

	private final int CHECK_TIME_PERIOD = 1000 * 60 * 5;
	private Future<?> checkTask = null;
	private List<EventTemplate> enabledEvents = new FastTable<>();

	TIntObjectHashMap<List<EventTemplate>> eventsForStartQuest = new TIntObjectHashMap<List<EventTemplate>>();
	TIntObjectHashMap<List<EventTemplate>> eventsForMaintainQuest = new TIntObjectHashMap<List<EventTemplate>>();

	private static class SingletonHolder {

		protected static final EventService instance = new EventService();
	}

	public static final EventService getInstance() {
		return SingletonHolder.instance;
	}

	private EventService() {
		if (EventsConfig.ENABLE_EVENT_SERVICE)
			start();
	}

	/**
	 * This method is called just after player logged in to the game.<br>
	 * <br>
	 * <b><font color='red'>NOTICE: </font>This method must not be called from anywhere else.</b>
	 */
	public void onPlayerLogin(Player player) {
		if (!EventsConfig.ENABLE_EVENT_SERVICE)
			return;

		List<Integer> activeStartQuests = new FastTable<Integer>();
		List<Integer> activeMaintainQuests = new FastTable<Integer>();
		TIntObjectHashMap<List<EventTemplate>> map1 = null;
		TIntObjectHashMap<List<EventTemplate>> map2 = null;

		synchronized (enabledEvents) {
			for (EventTemplate et : enabledEvents) {
				if (et.isActive()) {
					activeStartQuests.addAll(et.getStartableQuests());
					activeMaintainQuests.addAll(et.getMaintainableQuests());
				}
			}
			map1 = new TIntObjectHashMap<List<EventTemplate>>(eventsForStartQuest);
			map2 = new TIntObjectHashMap<List<EventTemplate>>(eventsForMaintainQuest);
		}

		StartOrMaintainQuests(player, activeStartQuests.listIterator(), map1, true);
		StartOrMaintainQuests(player, activeMaintainQuests.listIterator(), map2, false);

		activeStartQuests.clear();
		activeMaintainQuests.clear();
		map1.clear();
		map2.clear();
	}

	void StartOrMaintainQuests(Player player, ListIterator<Integer> questList, TIntObjectHashMap<List<EventTemplate>> templateMap, boolean start) {
		while (questList.hasNext()) {
			int questId = questList.next();
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			QuestEnv cookie = new QuestEnv(null, player, questId, 0);
			QuestStatus status = qs == null ? QuestStatus.START : qs.getStatus();

			if (QuestService.checkLevelRequirement(questId, player.getCommonData().getLevel())) {
				QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
				if (template.getRacePermitted() != null) {
					if (template.getRacePermitted().ordinal() != player.getCommonData().getRace().ordinal())
						continue;
				}

				if (template.getClassPermitted().size() != 0) {
					if (!template.getClassPermitted().contains(player.getCommonData().getPlayerClass()))
						continue;
				}

				if (template.getGenderPermitted() != null) {
					if (template.getGenderPermitted().ordinal() != player.getGender().ordinal())
						continue;
				}

				int requiredStartConditions = template.getRequiredConditionCount();
				int fulfilledStartConditions = 0;
				for (XMLStartCondition startCondition : template.getXMLStartConditions()) {
					if (startCondition.check(player, false)) {
						fulfilledStartConditions++;
					}
				}
				if (fulfilledStartConditions < requiredStartConditions) {
					continue;
				}

				if (qs != null) {
					if (qs.getCompleteTime() != null || status == QuestStatus.COMPLETE) {
						DateTime completed = null;
						if (qs.getCompleteTime() == null)
							completed = new DateTime(0);
						else
							completed = new DateTime(qs.getCompleteTime().getTime());

						if (templateMap.containsKey(questId)) {
							for (EventTemplate et : templateMap.get(questId)) {
								// recurring event, reset it
								if (et.getStartDate().isAfter(completed)) {
									if (start) {
										status = QuestStatus.START;
										qs.setQuestVar(0);
										qs.setCompleteCount(0);
										qs.setStatus(status);
									}
									break;
								}
							}
						}
					}
					// re-register quests
					if (status == QuestStatus.COMPLETE) {
						PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(questId, status, qs.getQuestVars().getQuestVars(), qs.getFlags()));
					} else
						QuestService.startEventQuest(cookie, status);
				} else if (start) {
					QuestService.startEventQuest(cookie, status);
				}
			}
		}
	}

	public void start() {
		if (checkTask != null)
			checkTask.cancel(true);

		checkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				checkEvents();
			}
		}, 0, CHECK_TIME_PERIOD);
	}

	public void stop() {
		if (checkTask != null) {
			checkTask.cancel(true);
			checkTask = null;
		}
	}

	private void checkEvents() {
		List<EventTemplate> newEnabledEvents = new FastTable<EventTemplate>();
		List<String> availableEventNames = DataManager.EVENT_DATA.getEventNames();
		List<String> enabledEventNames = EventsConfig.ENABLED_EVENTS.equals("*") ? availableEventNames : FastTable.of(EventsConfig.ENABLED_EVENTS.split(","));

		synchronized (enabledEvents) {
			for (EventTemplate et : enabledEvents) {
				if (et.isExpired() || !availableEventNames.contains(et.getName()) || !enabledEventNames.contains(et.getName()))
					et.Stop();
			}

			for (String eventName : enabledEventNames) {
				EventTemplate et = DataManager.EVENT_DATA.getEvent(eventName.trim());
				if (et != null) {
					newEnabledEvents.add(et);
					if (et.isActive())
						et.Start();
				}
			}

			enabledEvents.clear();
			eventsForStartQuest.clear();
			eventsForMaintainQuest.clear();
			enabledEvents.addAll(newEnabledEvents);
			updateQuestMap();
		}
	}

	private void updateQuestMap() {
		for (EventTemplate et : enabledEvents) {
			for (int qId : et.getStartableQuests()) {
				if (!eventsForStartQuest.containsKey(qId))
					eventsForStartQuest.put(qId, new FastTable<EventTemplate>());
				eventsForStartQuest.get(qId).add(et);
			}
			for (int qId : et.getMaintainableQuests()) {
				if (!eventsForMaintainQuest.containsKey(qId))
					eventsForMaintainQuest.put(qId, new FastTable<EventTemplate>());
				eventsForMaintainQuest.get(qId).add(et);
			}
		}
	}

	public boolean checkQuestIsActive(int questId) {
		synchronized (enabledEvents) {
			if (eventsForStartQuest.containsKey(questId) || eventsForMaintainQuest.containsKey(questId))
				return true;
		}
		return false;
	}

	public EventType getEventType() {
		if (EventsConfig.ENABLE_EVENT_SERVICE) {
			for (EventTemplate et : enabledEvents) {
				String theme = et.getTheme();
				if (theme != null) {
					EventType type = EventType.getEventType(theme);
					if (et.isActive() && !type.equals(EventType.NONE)) {
						return type;
					}
				}
			}
		}
		return EventType.NONE;
	}

	public List<EventTemplate> getEnabledEvents() {
		return enabledEvents;
	}
}
