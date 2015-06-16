package com.aionemu.gameserver.questEngine.handlers.template;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javolution.util.FastMap;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.handlers.models.QuestSkillData;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog, modified Bobobear
 */
public class SkillUse extends QuestHandler {

	private final int startNpc;
	private final int endNpc;
	private final FastMap<List<Integer>, QuestSkillData> qsd;

	public SkillUse(int questId, int startNpc, int endNpc, FastMap<List<Integer>, QuestSkillData> qsd) {
		super(questId);
		this.startNpc = startNpc;
		if (endNpc != 0) {
			this.endNpc = endNpc;
		}
		else {
			this.endNpc = startNpc;
		}
		this.qsd = qsd;
	}

	@Override
	public void register() {
		qe.registerQuestNpc(startNpc).addOnQuestStart(questId);
		qe.registerQuestNpc(startNpc).addOnTalkEvent(questId);
		if (endNpc != startNpc) {
			qe.registerQuestNpc(endNpc).addOnTalkEvent(questId);
		}
		for (List<Integer> skillIds : qsd.keySet()) {
			Iterator<Integer> iterator = skillIds.iterator();
			while (iterator.hasNext()) {
				int SkillId = iterator.next();
				qe.registerQuestSkill(SkillId, questId);
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
			if (targetId == startNpc) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			// TODO: check skill use count, see MonsterHunt.java how to get total count
			int var = qs.getQuestVarById(0);
			if (targetId == endNpc) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 10002);
				}
				else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
					changeQuestStep(env, var, var, true); // reward
					return sendQuestDialog(env, 5);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == endNpc) {
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
					}
					while (endVar > 0);
					total += 1;
					if (total <= qd.getEndVar()) {
						for (int varsUsed = qd.getVarNum(); varsUsed < varId; varsUsed++) {
							int value = total & 0x3F;
							total >>= 6;
							qs.setQuestVarById(varsUsed, value);
						}
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
			constantSpawns.add(startNpc);
			if (endNpc != 0)
				constantSpawns.add(endNpc);
		}
		return constantSpawns;
	}
}
