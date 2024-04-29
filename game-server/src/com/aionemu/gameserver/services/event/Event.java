package com.aionemu.gameserver.services.event;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.Config;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.templates.Guides.GuideTemplate;
import com.aionemu.gameserver.model.templates.QuestTemplate;
import com.aionemu.gameserver.model.templates.event.EventTemplate;
import com.aionemu.gameserver.model.templates.event.InventoryDrop;
import com.aionemu.gameserver.model.templates.quest.QuestCategory;
import com.aionemu.gameserver.model.templates.spawns.Spawn;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnMap;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION.ActionType;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.skillengine.model.Effect.ForceType;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.time.ServerTime;
import com.aionemu.gameserver.world.World;

/**
 * @author Neon
 */
public class Event {

	private static final Logger log = LoggerFactory.getLogger(Event.class);
	private static final String EFFECT_FORCE_TYPE_PREFIX = "[EVENT] ";

	private final EventTemplate eventTemplate;
	private final AtomicBoolean started = new AtomicBoolean();
	private EventBuffHandler eventBuffHandler;
	private Future<?> inventoryDropTask;
	private List<Runnable> onEventEndTasks;

	public Event(EventTemplate eventTemplate) {
		this.eventTemplate = eventTemplate;
	}

	public EventTemplate getEventTemplate() {
		return eventTemplate;
	}

	public static ForceType getOrCreateEffectForceType(String identifier) {
		return ForceType.getInstance(EFFECT_FORCE_TYPE_PREFIX + identifier);
	}

	public static boolean isEventEffectForceType(ForceType forceType) {
		return forceType != null && forceType.getName().startsWith(EFFECT_FORCE_TYPE_PREFIX);
	}

	public void start() {
		if (!started.compareAndSet(false, true))
			return;
		if (eventTemplate.hasConfigProperties()) {
			try {
				Config.load(eventTemplate.loadConfigProperties());
			} catch (IOException e) {
				log.error("Could not load config properties of event " + getEventTemplate().getName(), e);
				started.compareAndSet(true, false);
				return;
			}
		}
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
						if (spawn.isCustom())
							despawnNonEventSpawns(spawn.getNpcId(), map.getMapId(), instanceId);
					}
					log.info("Spawned event objects in " + map.getMapId() + " [" + instanceId + "]: " + spawnCount + " (" + eventTemplate.getName() + ")");
				}
			}
			DataManager.SPAWNS_DATA.afterUnmarshal(null, null);
			DataManager.SPAWNS_DATA.clearTemplates();
		}

		InventoryDrop inventoryDrop = eventTemplate.getInventoryDrop();
		if (inventoryDropTask == null && inventoryDrop != null) {
			inventoryDropTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
				World.getInstance().forEachPlayer(player -> {
					if (player.getLevel() >= inventoryDrop.getStartLevel())
						// TODO: check the exact type in retail
						ItemService.addItem(player, inventoryDrop.getItemId(), inventoryDrop.getCount(), true,
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

		if (eventTemplate.getBuffs() != null)
			eventBuffHandler = new EventBuffHandler(eventTemplate.getName(), eventTemplate.getBuffs());

		World.getInstance().forEachPlayer(player -> { // simulate login on event start
			onPlayerLogin(player);
			onEnterMap(player);
		});

		log.info("Started event: " + eventTemplate.getName());
	}

	public void stop() {
		started.set(false);
		if (eventTemplate.hasConfigProperties()) {
			Config.load();
		}
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

		synchronized (this) {
			if (onEventEndTasks != null) {
				for (Runnable task : onEventEndTasks) {
					try {
						task.run();
					} catch (Exception e) {
						log.error("Could not execute task on end of event " + getEventTemplate().getName(), e);
					}
				}
				onEventEndTasks = null;
			}
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

		if (eventBuffHandler != null) {
			eventBuffHandler.onEventStop();
			eventBuffHandler = null;
		}

		log.info("Stopped event: " + eventTemplate.getName());
	}

	public ForceType getEffectForceType() {
		return eventBuffHandler == null ? null : eventBuffHandler.getEffectForceType();
	}

	public void onTimeChanged(ZonedDateTime now) {
		if (eventBuffHandler != null)
			eventBuffHandler.onTimeChanged(now);
	}

	public void onPlayerLogin(Player player) {
		startOrMaintainQuests(player);
	}

	public void onEnteredTeam(Player player, TemporaryPlayerTeam<? extends TeamMember<Player>> team) {
		if (eventBuffHandler != null)
			eventBuffHandler.onEnteredTeam(player, team);
	}

	public void onLeftTeam(Player player, TemporaryPlayerTeam<? extends TeamMember<Player>> team) {
		if (eventBuffHandler != null)
			eventBuffHandler.onLeftTeam(player, team);
	}

	public void onEnterMap(Player player) {
		if (eventBuffHandler != null)
			eventBuffHandler.onEnterMap(player);
	}

	public void onPveKill(Player killer, Npc victim) {
		if (eventBuffHandler != null)
			eventBuffHandler.onPveKill(killer, victim);
	}

	public void onPvpKill(Player killer, Player victim) {
		if (eventBuffHandler != null)
			eventBuffHandler.onPvpKill(killer, victim);
	}

	private void startOrMaintainQuests(Player player) {
		for (int startableQuestId : eventTemplate.getStartableQuests()) {
			if (isAllowedToStartEventQuest(player, startableQuestId)) {
				QuestState qs = player.getQuestStateList().getQuestState(startableQuestId);
				if (qs == null) {
					qs = new QuestState(startableQuestId, QuestStatus.START);
					player.getQuestStateList().addQuest(startableQuestId, qs);
					PacketSendUtility.sendPacket(player, new SM_QUEST_ACTION(ActionType.ADD, qs));
				} else if (qs.getStatus() != QuestStatus.START && qs.getCompleteCount() > 0 && eventTemplate.getStartDate() != null) {
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

	private void despawnNonEventSpawns(int npcId, int mapId, int instanceId) {
		World.getInstance().getWorldMap(mapId).getWorldMapInstance(instanceId).getNpcs(npcId).forEach(npc -> {
			if (npc.getSpawn() != null && !npc.getSpawn().isEventSpawn() && !npc.getController().hasScheduledTask(TaskId.DECAY)) {
				if (npc.getController().delete() && !RespawnService.hasRespawnTask(npc))
					addOnEventEndTask(new RespawnService.RespawnTask(npc));
			}
		});
	}

	public boolean addOnEventEndTask(Runnable task) {
		if (!started.get())
			return false;
		synchronized (this) {
			if (onEventEndTasks == null)
				onEventEndTasks = new ArrayList<>();
			onEventEndTasks.add(task);
			return true;
		}
	}
}
