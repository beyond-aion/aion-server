package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import javolution.util.FastMap;

/**
 * @author vlog
 * @modified Pad
 */
public class KillSpawned extends QuestHandler {

	private final Set<Integer> startNpcIds = new HashSet<>();
	private final Set<Integer> endNpcIds = new HashSet<>();
	private final Set<Integer> spawnerObjectIds = new HashSet<>();
	private final FastMap<List<Integer>, Monster> spawnedMonsters;
	private final boolean isDataDriven;

	public KillSpawned(int questId, List<Integer> startNpcIds, List<Integer> endNpcIds, FastMap<List<Integer>, Monster> spawnedMonsters) {
		super(questId);
		if (startNpcIds != null)
			this.startNpcIds.addAll(startNpcIds);
		if (endNpcIds != null)
			this.endNpcIds.addAll(endNpcIds);
		else
			this.endNpcIds.addAll(this.startNpcIds);
		this.spawnedMonsters = spawnedMonsters;
		for (Monster m : spawnedMonsters.values())
			spawnerObjectIds.add(m.getSpawnerNpcId());
		this.isDataDriven = DataManager.QUEST_DATA.getQuestById(questId).isDataDriven();
	}

	@Override
	public void register() {
		for (Integer startNpcId : startNpcIds) {
			qe.registerQuestNpc(startNpcId).addOnQuestStart(questId);
			qe.registerQuestNpc(startNpcId).addOnTalkEvent(questId);
		}
		if (!endNpcIds.equals(startNpcIds)) {
			for (Integer endNpcId : endNpcIds)
				qe.registerQuestNpc(endNpcId).addOnTalkEvent(questId);
		}
		for (List<Integer> spawnedMonsterIds : spawnedMonsters.keySet()) {
			for (Integer spawnedMonsterId : spawnedMonsterIds)
				qe.registerQuestNpc(spawnedMonsterId).addOnKillEvent(questId);
		}
		for (Integer spawnerObjectId : spawnerObjectIds)
			qe.registerQuestNpc(spawnerObjectId).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (startNpcIds.isEmpty() || startNpcIds.contains(targetId)) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, isDataDriven ? 4762 : 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (spawnerObjectIds.contains(targetId)) {
				if (dialog == DialogAction.USE_OBJECT) {
					int monsterId = 0;
					for (Monster m : spawnedMonsters.values()) {
						if (m.getSpawnerNpcId() == targetId) {
							monsterId = m.getNpcIds().get(0);
							break;
						}
					}
					if (monsterId == 0)
						return false;
					SpawnSearchResult searchResult = DataManager.SPAWNS_DATA.getFirstSpawnByNpcId(player.getWorldId(), targetId);
					QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), monsterId, searchResult.getSpot().getX(),
						searchResult.getSpot().getY(), searchResult.getSpot().getZ(), searchResult.getSpot().getHeading());
					return true;
				}
			} else {
				for (Monster m : spawnedMonsters.values()) {
					if (m.getEndVar() > qs.getQuestVarById(m.getVar())) {
						return false;
					}
				}
				if (endNpcIds.contains(targetId)) {
					if (dialog == DialogAction.QUEST_SELECT) {
						return sendQuestDialog(env, 10002);
					} else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
						return sendQuestDialog(env, 5);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (endNpcIds.contains(targetId)) {
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
						for (Monster n : spawnedMonsters.values()) {
							if (qs.getQuestVarById(n.getVar()) < n.getEndVar()) {
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
}
