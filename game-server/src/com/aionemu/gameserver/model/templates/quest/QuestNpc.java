package com.aionemu.gameserver.model.templates.quest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aionemu.gameserver.questEngine.QuestEngine;

/**
 * @author MrPoke
 */
public class QuestNpc {

	private final Set<Integer> onQuestStart;
	private final List<Integer> onKillEvent;
	private final List<Integer> onTalkEvent;
	private final List<Integer> onAttackEvent;
	private final List<Integer> onAddAggroListEvent;
	private final List<Integer> onAtDistanceEvent;
	private final int npcId;
	private final int questRange;

	public QuestNpc(int npcId, int questRange) {
		this.npcId = npcId;
		this.questRange = questRange;
		onQuestStart = new HashSet<>(0);
		onKillEvent = new ArrayList<>();
		onTalkEvent = new ArrayList<>();
		onAttackEvent = new ArrayList<>();
		onAddAggroListEvent = new ArrayList<>();
		onAtDistanceEvent = new ArrayList<>();
	}

	public QuestNpc(int npcId) {
		this(npcId, 20);
	}

	public void addOnQuestStart(int questId) {
		if (!onQuestStart.contains(questId)) {
			onQuestStart.add(questId);
		}
	}

	public Set<Integer> getOnQuestStart() {
		return onQuestStart;
	}

	public void addOnAttackEvent(int questId) {
		if (!onAttackEvent.contains(questId)) {
			onAttackEvent.add(questId);
		}
	}

	public List<Integer> getOnAttackEvent() {
		return onAttackEvent;
	}

	public void addOnKillEvent(int questId) {
		if (!onKillEvent.contains(questId)) {
			onKillEvent.add(questId);
			QuestEngine.getInstance().registerCanAct(questId, npcId);
		}
	}

	public List<Integer> getOnKillEvent() {
		return onKillEvent;
	}

	public void addOnTalkEvent(int questId) {
		if (!onTalkEvent.contains(questId)) {
			onTalkEvent.add(questId);
			QuestEngine.getInstance().registerCanAct(questId, npcId);
		}
	}

	public List<Integer> getOnTalkEvent() {
		return onTalkEvent;
	}

	public void addOnAddAggroListEvent(int questId) {
		if (!onAddAggroListEvent.contains(questId)) {
			onAddAggroListEvent.add(questId);
			QuestEngine.getInstance().registerCanAct(questId, npcId);
		}
	}

	public List<Integer> getOnAddAggroListEvent() {
		return onAddAggroListEvent;
	}

	public void addOnAtDistanceEvent(int questId) {
		if (!onAtDistanceEvent.contains(questId)) {
			onAtDistanceEvent.add(questId);
			QuestEngine.getInstance().registerCanAct(questId, npcId);
		}
	}

	public List<Integer> getOnDistanceEvent() {
		return onAtDistanceEvent;
	}

	public int getNpcId() {
		return npcId;
	}

	public int getQuestRange() {
		return questRange;
	}

	/**
	 * @return A set of all quest ids which have been registered from quest handlers for this quest npc
	 */
	public Set<Integer> findAllRegisteredQuestIds() {
		Stream<Integer> questIds = Stream.of(onQuestStart, onTalkEvent, onAtDistanceEvent, onAddAggroListEvent, onAttackEvent, onKillEvent)
			.flatMap(c -> c.stream());
		return questIds.collect(Collectors.toSet());
	}
}
