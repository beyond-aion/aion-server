package com.aionemu.gameserver.questEngine.handlers.template;

import static com.aionemu.gameserver.model.DialogAction.QUEST_SELECT;
import static com.aionemu.gameserver.model.DialogAction.USE_OBJECT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.models.Monster;
import com.aionemu.gameserver.questEngine.handlers.models.XmlQuestData;
import com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events.OnEnterWorldEvent;
import com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events.OnEnterZoneEvent;
import com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events.OnItemUseEvent;
import com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events.OnKillEvent;
import com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events.OnLevelUpEvent;
import com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events.OnTalkEvent;
import com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.events.OnTimerEndEvent;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Mr.Poke, Bobobear, Pad
 */
public class XmlQuest extends AbstractTemplateQuestHandler {

	private static final Logger log = LoggerFactory.getLogger(XmlQuest.class);

	private final Set<Integer> startNpcIds = new HashSet<>();
	private final Set<Integer> endNpcIds = new HashSet<>();
	private final List<OnTalkEvent> onTalkEvents = new ArrayList<>();
	private final List<OnKillEvent> onKillEvents = new ArrayList<>();
	private final List<OnEnterZoneEvent> onEnterZoneEvents = new ArrayList<>();
	private final List<OnItemUseEvent> onItemUseEvents = new ArrayList<>();
	private final List<OnTimerEndEvent> onTimerEndEvents = new ArrayList<>();
	private final List<OnEnterWorldEvent> onEnterWorldEvents = new ArrayList<>();
	private final List<OnLevelUpEvent> onLevelUpEvents = new ArrayList<>();
	private final boolean isDataDriven;

	public XmlQuest(XmlQuestData data) {
		super(data.getId());
		if (data.getStartNpcIds() != null)
			this.startNpcIds.addAll(data.getStartNpcIds());
		if (data.getEndNpcIds() != null)
			this.endNpcIds.addAll(data.getEndNpcIds());
		else
			this.endNpcIds.addAll(this.startNpcIds);
		if (data.getOnTalkEvents() != null)
			this.onTalkEvents.addAll(data.getOnTalkEvents());
		if (data.getOnKillEvents() != null)
			this.onKillEvents.addAll(data.getOnKillEvents());
		if (data.getOnEnterZoneEvents() != null)
			this.onEnterZoneEvents.addAll(data.getOnEnterZoneEvents());
		if (data.getOnItemUseEvents() != null)
			this.onItemUseEvents.addAll(data.getOnItemUseEvents());
		if (data.getOnTimerEndEvents() != null)
			this.onTimerEndEvents.addAll(data.getOnTimerEndEvents());
		if (data.getOnEnterWorldEvents() != null)
			this.onEnterWorldEvents.addAll(data.getOnEnterWorldEvents());
		if (data.getOnLevelUpEvents() != null)
			this.onLevelUpEvents.addAll(data.getOnLevelUpEvents());
		isDataDriven = DataManager.QUEST_DATA.getQuestById(questId).isDataDriven();
	}

	@Override
	public void register() {
		for (Integer startNpcId : startNpcIds) {
			if (!npcExists(startNpcId, "start_npc_ids"))
				continue;
			qe.registerQuestNpc(startNpcId).addOnQuestStart(questId);
			qe.registerQuestNpc(startNpcId).addOnTalkEvent(questId);
		}
		if (!endNpcIds.equals(startNpcIds)) {
			for (Integer endNpcId : endNpcIds) {
				if (npcExists(endNpcId, "end_npc_ids"))
					qe.registerQuestNpc(endNpcId).addOnTalkEvent(questId);
			}
		}
		for (OnTalkEvent onTalkEvent : onTalkEvents) {
			for (Integer npcId : onTalkEvent.getIds()) {
				if (npcExists(npcId, "on_talk_event"))
					qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
			}
		}
		for (OnKillEvent onKillEvent : onKillEvents) {
			for (Monster monster : onKillEvent.getMonsters()) {
				for (Integer monsterId : monster.getNpcIds()) {
					if (npcExists(monsterId, "on_kill_event"))
						qe.registerQuestNpc(monsterId).addOnKillEvent(questId);
				}
			}
		}
		for (OnEnterZoneEvent onEnterZoneEvent : onEnterZoneEvents) {
			for (String zone : onEnterZoneEvent.getZones()) {
				ZoneName zoneName = ZoneName.get(zone);
				if (zoneName != ZoneName.NONE) // unknown zone names are logged by ZoneName.get
					qe.registerOnEnterZone(zoneName, questId);
			}
		}
		for (OnItemUseEvent onItemUseEvent : onItemUseEvents) {
			for (Integer itemId : onItemUseEvent.getItemIds()) {
				if (DataManager.ITEM_DATA.getItemTemplate(itemId) == null)
					log.warn("Quest {} references item {} in on_item_use_event, which doesn't exist", questId, itemId);
				else
					qe.registerQuestItem(itemId, questId);
			}
		}
		if (!onTimerEndEvents.isEmpty()) {
			qe.registerOnQuestTimerEnd(questId);
			qe.registerOnInvisibleTimerEnd(questId);
		}
		if (!onEnterWorldEvents.isEmpty())
			qe.registerOnEnterWorld(questId);
		if (!onLevelUpEvents.isEmpty())
			qe.registerOnLevelChanged(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		//env.setQuestId(questId);
		for (OnTalkEvent onTalkEvent : onTalkEvents) {
			if (onTalkEvent.operate(env))
				return true;
		}

		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		
		if (qs == null || qs.isStartable()) {
			if (startNpcIds.contains(targetId)) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, isDataDriven ? 4762 : 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD && endNpcIds.contains(targetId)) {
			// data driven quests let the npc comment on the result before the reward window opens
			if (isDataDriven && (env.getDialogActionId() == QUEST_SELECT || env.getDialogActionId() == USE_OBJECT))
				return sendQuestDialog(env, 10002);
			return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		//env.setQuestId(questId);
		for (OnKillEvent onKillEvent : onKillEvents) {
			if (onKillEvent.operate(env))
				return true;
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		for (OnEnterZoneEvent onEnterZoneEvent : onEnterZoneEvents) {
			if (onEnterZoneEvent.operate(env, zoneName))
				return true;
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		for (OnItemUseEvent onItemUseEvent : onItemUseEvents) {
			if (onItemUseEvent.operate(env, item.getItemId()))
				return HandlerResult.SUCCESS;
		}
		return HandlerResult.UNKNOWN;
	}

	@Override
	public boolean isWaitingForInteractionWith(Player player, int npcId) {
		QuestEnv env = new QuestEnv(null, player, questId);
		for (OnTalkEvent onTalkEvent : onTalkEvents) {
			if (onTalkEvent.isWaitingFor(env, npcId))
				return true;
		}
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable())
			return startNpcIds.contains(npcId);
		return qs.getStatus() == QuestStatus.REWARD && endNpcIds.contains(npcId);
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		for (OnEnterWorldEvent onEnterWorldEvent : onEnterWorldEvents) {
			if (onEnterWorldEvent.operate(env))
				return true;
		}
		return false;
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		QuestEnv env = new QuestEnv(null, player, questId);
		for (OnLevelUpEvent onLevelUpEvent : onLevelUpEvents) {
			if (onLevelUpEvent.operate(env))
				return;
		}
	}

	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		return operateTimerEndEvents(env);
	}

	@Override
	public boolean onInvisibleTimerEndEvent(QuestEnv env) {
		return operateTimerEndEvents(env);
	}

	private boolean npcExists(int npcId, String declaredIn) {
		if (DataManager.NPC_DATA.getNpcTemplate(npcId) != null)
			return true;
		log.warn("Quest {} references npc {} in {}, which doesn't exist", questId, npcId, declaredIn);
		return false;
	}

	private boolean operateTimerEndEvents(QuestEnv env) {
		for (OnTimerEndEvent onTimerEndEvent : onTimerEndEvents) {
			if (onTimerEndEvent.operate(env))
				return true;
		}
		return false;
	}
}
