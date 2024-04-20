package com.aionemu.gameserver.questEngine.handlers.template;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.questEngine.handlers.models.Monster;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog, Pad
 */
public class KillSpawned extends AbstractTemplateQuestHandler {

	private final Set<Integer> startNpcIds = new HashSet<>();
	private final Set<Integer> endNpcIds = new HashSet<>();
	private final Set<Integer> spawnerObjectIds = new HashSet<>();
	private final List<Monster> spawnedMonsters;
	private final boolean isDataDriven;

	public KillSpawned(int questId, List<Integer> startNpcIds, List<Integer> endNpcIds, List<Monster> spawnedMonsters) {
		super(questId);
		if (startNpcIds != null)
			this.startNpcIds.addAll(startNpcIds);
		if (endNpcIds != null)
			this.endNpcIds.addAll(endNpcIds);
		else
			this.endNpcIds.addAll(this.startNpcIds);
		this.spawnedMonsters = spawnedMonsters == null ? Collections.emptyList() : spawnedMonsters;
		for (Monster m : this.spawnedMonsters)
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
		for (Monster spawnedMonster : spawnedMonsters) {
			for (Integer spawnedMonsterId : spawnedMonster.getNpcIds())
				qe.registerQuestNpc(spawnedMonsterId).addOnKillEvent(questId);
		}
		for (Integer spawnerObjectId : spawnerObjectIds)
			qe.registerQuestNpc(spawnerObjectId).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (startNpcIds.isEmpty() || startNpcIds.contains(targetId)) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, isDataDriven ? 4762 : 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (spawnerObjectIds.contains(targetId)) {
				if (dialogActionId == USE_OBJECT) {
					int monsterId = 0;
					for (Monster m : spawnedMonsters) {
						if (m.getSpawnerNpcId() == targetId) {
							monsterId = m.getNpcIds().get(0);
							break;
						}
					}
					if (monsterId == 0)
						return false;
					SpawnSpotTemplate spot = DataManager.SPAWNS_DATA.getFirstSpawnByNpcId(player.getWorldId(), targetId).getSpot();
					spawnForFiveMinutes(monsterId, player.getWorldMapInstance(), spot.getX(), spot.getY(), spot.getZ(), spot.getHeading());
					return true;
				}
			} else {
				for (Monster m : spawnedMonsters) {
					if (m.getEndVar() > qs.getQuestVarById(m.getVar())) {
						return false;
					}
				}
				if (endNpcIds.contains(targetId)) {
					if (dialogActionId == QUEST_SELECT) {
						return sendQuestDialog(env, 10002);
					} else if (dialogActionId == SELECT_QUEST_REWARD) {
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
			for (Monster m : spawnedMonsters) {
				if (m.getNpcIds().contains(env.getTargetId())) {
					if (qs.getQuestVarById(m.getVar()) < m.getEndVar()) {
						qs.setQuestVarById(m.getVar(), qs.getQuestVarById(m.getVar()) + 1);
						for (Monster n : spawnedMonsters) {
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
