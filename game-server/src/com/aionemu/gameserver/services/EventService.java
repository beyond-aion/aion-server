package com.aionemu.gameserver.services;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EventType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.quest.QuestCategory;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION.ActionType;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastTable;

/**
 * @author Rolandas
 * @modified Neon
 */
public class EventService {

	private final int CHECK_TIME_PERIOD = 1000 * 60 * 5;
	private Future<?> checkTask = null;
	private List<EventTemplate> enabledEvents = new FastTable<>();
	TIntObjectHashMap<List<EventTemplate>> eventsForStartQuest = new TIntObjectHashMap<>();
	TIntObjectHashMap<List<EventTemplate>> eventsForMaintainQuest = new TIntObjectHashMap<>();

	private static class SingletonHolder {

		protected static final EventService instance = new EventService();
	}

	public static final EventService getInstance() {
		return SingletonHolder.instance;
	}

	private EventService() {
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

		List<Integer> activeStartQuests = new FastTable<>();
		List<Integer> activeMaintainQuests = new FastTable<>();
		TIntObjectHashMap<List<EventTemplate>> map1;
		TIntObjectHashMap<List<EventTemplate>> map2;

		synchronized (enabledEvents) {
			for (EventTemplate et : enabledEvents) {
				if (et.isActive()) {
					activeStartQuests.addAll(et.getStartableQuests());
					activeMaintainQuests.addAll(et.getMaintainableQuests());
				}
			}
			map1 = new TIntObjectHashMap<>(eventsForStartQuest);
			map2 = new TIntObjectHashMap<>(eventsForMaintainQuest);
		}

		startOrMaintainQuests(player, activeStartQuests, map1, true);
		startOrMaintainQuests(player, activeMaintainQuests, map2, false);
	}

	private void startOrMaintainQuests(Player player, List<Integer> questList, TIntObjectHashMap<List<EventTemplate>> templateMap, boolean start) {
		for (Integer questId : questList) {
			QuestTemplate template = DataManager.QUEST_DATA.getQuestById(questId);
			if (template.getCategory() != QuestCategory.EVENT)
				continue;

			if (!QuestService.checkStartConditions(player, questId, false, 0, true, true, false))
				continue;

			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null) {
				if (qs.getLastCompleteTime() != null && qs.getCompleteCount() > 0 && templateMap.containsKey(questId)) {
					ZonedDateTime completeTime = qs.getLastCompleteTime().toInstant().atZone(GSConfig.TIME_ZONE.toZoneId());
					for (EventTemplate et : templateMap.get(questId)) {
						if (et.getStartDate().isAfter(completeTime)) { // recurring event, reset it
							qs.setCompleteCount(0); // reset complete count if quest was last completed on a previous event, so it can be done again
							if (start && qs.getStatus() != QuestStatus.START) {
								ActionType actionType = qs.getStatus() == QuestStatus.COMPLETE ? ActionType.ADD : ActionType.UPDATE;
								qs.setStatus(QuestStatus.START);
								qs.setQuestVar(0);
								qs.setReward(null);
								PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(actionType, qs));
							}
							break;
						}
					}
				}
			} else {
				if (start) {
					player.getQuestStateList().addQuest(questId, new QuestState(questId, QuestStatus.START));
					PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(ActionType.ADD, qs));
				}
			}
		}
	}

	public boolean start() {
		if (!EventsConfig.ENABLE_EVENT_SERVICE || checkTask != null)
			return false;

		checkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> checkEvents(), 0, CHECK_TIME_PERIOD);
		return true;
	}

	public void stop() {
		if (checkTask != null) {
			checkTask.cancel(true);
			checkTask = null;
		}
	}

	private void checkEvents() {
		List<EventTemplate> newEnabledEvents = new FastTable<>();
		List<String> availableEventNames = DataManager.EVENT_DATA.getEventNames();
		List<String> enabledEventNames = EventsConfig.ENABLED_EVENTS.equals("*") ? availableEventNames
			: FastTable.of(EventsConfig.ENABLED_EVENTS.split("\\s*,\\s*"));

		synchronized (enabledEvents) {
			for (EventTemplate et : enabledEvents) {
				if (et.isExpired() || !availableEventNames.contains(et.getName()) || !enabledEventNames.contains(et.getName()))
					et.stop();
			}

			for (String eventName : enabledEventNames) {
				EventTemplate et = DataManager.EVENT_DATA.getEvent(eventName);
				if (et != null) {
					newEnabledEvents.add(et);
					if (et.isActive())
						et.start();
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
				if (et.isStarted() && et.getTheme() != null)
					return EventType.getEventType(et.getTheme());
			}
		}
		return EventType.NONE;
	}

	public List<EventTemplate> getEnabledEvents() {
		return enabledEvents;
	}
}
