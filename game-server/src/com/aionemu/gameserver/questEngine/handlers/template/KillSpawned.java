package com.aionemu.gameserver.questEngine.handlers.template;

import gnu.trove.list.array.TIntArrayList;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javolution.util.FastMap;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnSearchResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.handlers.models.Monster;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author vlog
 */
public class KillSpawned extends QuestHandler {

	private final Set<Integer> startNpcs = new HashSet<Integer>();
	private final Set<Integer> endNpcs = new HashSet<Integer>();
	private final FastMap<List<Integer>, Monster> spawnedMonsters;
	private TIntArrayList spawnerObjects;

	public KillSpawned(int questId, List<Integer> startNpcIds, List<Integer> endNpcIds, FastMap<List<Integer>, Monster> spawnedMonsters) {
		super(questId);
		this.startNpcs.addAll(startNpcIds);
		this.startNpcs.remove(0);
		if (endNpcIds == null) {
			this.endNpcs.addAll(startNpcs);
		}
		else {
			this.endNpcs.addAll(endNpcIds);
			this.endNpcs.remove(0);
		}
		this.spawnedMonsters = spawnedMonsters;
		this.spawnerObjects = new TIntArrayList();
		for (Monster m : spawnedMonsters.values()) {
			spawnerObjects.add(m.getSpawnerObject());
		}
	}

	@Override
	protected void onWorkItemsLoaded() {
		// Have strange work items as Bait_1, Bait_2... Don't use.
		if (workItems == null)
			return;
		workItems.clear();
		workItems = null;
	}

	@Override
	public void register() {
		Iterator<Integer> iterator = startNpcs.iterator();
		while (iterator.hasNext()) {
			int startNpc = iterator.next();
			qe.registerQuestNpc(startNpc).addOnQuestStart(getQuestId());
			qe.registerQuestNpc(startNpc).addOnTalkEvent(getQuestId());
		}
		for (List<Integer> spawnedMonsterIds : spawnedMonsters.keySet()) {
			iterator = spawnedMonsterIds.iterator();
			while (iterator.hasNext()) {
				int spawnedMonsterId = iterator.next();
				qe.registerQuestNpc(spawnedMonsterId).addOnKillEvent(questId);
			}
		}
		iterator = endNpcs.iterator();
		while (iterator.hasNext()) {
			int endNpc = iterator.next();
			qe.registerQuestNpc(endNpc).addOnTalkEvent(getQuestId());
		}
		for (int i = 0; i < spawnerObjects.size(); i++) {
			qe.registerQuestNpc(spawnerObjects.get(i)).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (startNpcs.isEmpty() || startNpcs.contains(targetId)) {
				if (env.getDialog() == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (spawnerObjects.contains(targetId)) {
				if (env.getDialog() == DialogAction.USE_OBJECT) {
					int monsterId = 0;
					for (Monster m : spawnedMonsters.values()) {
						if (m.getSpawnerObject() == targetId) {
							monsterId = m.getNpcIds().get(0);
							break;
						}
					}

					SpawnSearchResult searchResult = DataManager.SPAWNS_DATA2.getFirstSpawnByNpcId(player.getWorldId(), targetId);
					QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), monsterId, searchResult.getSpot().getX(), searchResult
						.getSpot().getY(), searchResult.getSpot().getZ(), searchResult.getSpot().getHeading());
					return true;
				}
			}
			else {
				for (Monster mi : spawnedMonsters.values()) {
					if (mi.getEndVar() > qs.getQuestVarById(mi.getVar())) {
						return false;
					}
				}

				if (endNpcs.contains(targetId)) {
					if (env.getDialog() == DialogAction.QUEST_SELECT) {
						return sendQuestDialog(env, 10002);
					}
					else if (env.getDialog() == DialogAction.SELECT_QUEST_REWARD) {
						return sendQuestDialog(env, 5);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (endNpcs.contains(targetId)) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			for (Monster m : spawnedMonsters.values()) {
				if (m.getNpcIds().contains(env.getTargetId())) {
					if (qs.getQuestVarById(m.getVar()) < m.getEndVar()) {
						qs.setQuestVarById(m.getVar(), qs.getQuestVarById(m.getVar()) + 1);
						for (Monster mi : spawnedMonsters.values()) {
							if (qs.getQuestVarById(mi.getVar()) < mi.getEndVar()) {
								updateQuestStatus(env);
								return true;
							}
						}
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public HashSet<Integer> getNpcIds() {
		if (constantSpawns == null) {
			constantSpawns = new HashSet<>();
			if (startNpcs != null)
				constantSpawns.addAll(startNpcs);
			if (endNpcs != null)
				constantSpawns.addAll(endNpcs);
		}
		return constantSpawns;
	}

}
