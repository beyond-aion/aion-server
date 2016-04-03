package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javolution.util.FastMap;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.handlers.models.QuestSkillData;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog
 * @modified Bobobear, Pad
 */
public class SkillUse extends QuestHandler {

	private final Set<Integer> startNpcIds = new HashSet<>();
	private final Set<Integer> endNpcIds = new HashSet<>();
	private final FastMap<List<Integer>, QuestSkillData> qsd;

	public SkillUse(int questId, List<Integer> startNpcIds, List<Integer> endNpcIds, FastMap<List<Integer>, QuestSkillData> qsd) {
		super(questId);
		this.startNpcIds.addAll(startNpcIds);
		this.startNpcIds.remove(0);
		if (endNpcIds != null) {
			this.endNpcIds.addAll(endNpcIds);
			this.endNpcIds.remove(0);
		} else {
			this.endNpcIds.addAll(this.startNpcIds);
		}
		this.qsd = qsd;
	}

	@Override
	public void register() {
		Iterator<Integer> iterator = startNpcIds.iterator();
		while (iterator.hasNext()) {
			int startNpc = iterator.next();
			qe.registerQuestNpc(startNpc).addOnQuestStart(questId);
			qe.registerQuestNpc(startNpc).addOnTalkEvent(questId);
		}
		if (!startNpcIds.equals(endNpcIds)) {
			iterator = endNpcIds.iterator();
			while (iterator.hasNext()) {
				int endNpc = iterator.next();
				qe.registerQuestNpc(endNpc).addOnTalkEvent(questId);
			}
		}
		for (List<Integer> skillIds : qsd.keySet()) {
			iterator = skillIds.iterator();
			while (iterator.hasNext()) {
				int skillId = iterator.next();
				qe.registerQuestSkill(skillId, questId);
			}
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (startNpcIds.contains(targetId)) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			// TODO: check skill use count, see MonsterHunt.java how to get total count
			int var = qs.getQuestVarById(0);
			if (endNpcIds.contains(targetId)) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 10002);
				} else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
					changeQuestStep(env, var, var, true); // reward
					return sendQuestDialog(env, 5);
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
	public boolean onUseSkillEvent(QuestEnv env, int skillId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			byte rewardCount = 0;
			boolean success = false;
			for (QuestSkillData qd : qsd.values()) {
				if (qd.getSkillIds().contains(skillId)) {
					int endVar = qd.getEndVar();
					int varId = qd.getVarNum();
					int total = 0;
					do {
						int currentVar = qs.getQuestVarById(varId);
						total += currentVar << ((varId - qd.getVarNum()) * 6);
						endVar >>= 6;
						varId++;
					} while (endVar > 0);
					total += 1;
					if (total <= qd.getEndVar()) {
						for (int varsUsed = qd.getVarNum(); varsUsed < varId; varsUsed++) {
							int value = total & 0x3F;
							total >>= 6;
							qs.setQuestVarById(varsUsed, value);
						}
						if (qs.getQuestVarById(qd.getVarNum()) == qd.getEndVar())
							rewardCount++;
						updateQuestStatus(env);
						success = true;
					}
				}
			}
			if (rewardCount == qsd.size()) {
				if (qs.getQuestVarById(0) == 0)
					qs.setQuestVarById(0, 1);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
			}
			return success;
		}
		return false;
	}

	@Override
	public HashSet<Integer> getNpcIds() {
		if (constantSpawns == null) {
			constantSpawns = new HashSet<>();
			constantSpawns.addAll(startNpcIds);
			if (!startNpcIds.equals(endNpcIds))
				constantSpawns.addAll(endNpcIds);
		}
		return constantSpawns;
	}
}
