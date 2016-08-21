package quest.aturam_sky_fortress;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Luzien
 * @reworked Pad
 */
public class _28302DocumentSaved extends QuestHandler {

	private static final int questId = 28302;
	private int[] npcIds = { 799530, 730375 };
	private int[] generatorIds = { 702650, 702651, 702652, 702653, 702654 };
	private int popuchinId = 217373;

	public _28302DocumentSaved() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(npcIds[0]).addOnQuestStart(questId);
		for (int npcId : npcIds)
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		for (int generatorId : generatorIds)
			qe.registerQuestNpc(generatorId).addOnKillEvent(questId);
		qe.registerQuestNpc(popuchinId).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == npcIds[0]) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else if (dialog == DialogAction.QUEST_ACCEPT_1) {
					playQuestMovie(env, 469);
					return sendQuestStartDialog(env);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == npcIds[1]) {
				if (qs.getQuestVarById(0) == 5) {
					switch (dialog) {
						case USE_OBJECT:
							return sendQuestDialog(env, 1352);
						case SET_SUCCEED:
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						default:
							return sendQuestDialog(env, 2716);
					}
				}
			} else if (targetId == npcIds[0]) {
				return sendQuestDialog(env, 1004);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == npcIds[0]) {
				if (dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				if (dialog == DialogAction.SELECT_QUEST_REWARD) {
					removeQuestItem(env, 182212101, 1);
					return sendQuestDialog(env, 5);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.getStatus() != QuestStatus.START) {
			return false;
		}

		int targetId = env.getTargetId();
		int var0 = qs.getQuestVarById(0);

		if (var0 <= 4) {
			for (int generatorId : generatorIds) {
				if (targetId == generatorId) {
					qs.setQuestVarById(0, var0 + 1);
					updateQuestStatus(env);
					return true;
				}
			}
		} else if (var0 == 5) {
			if (targetId == popuchinId)
				QuestService.spawnQuestNpc(300240000, player.getInstanceId(), 730375, 374.9f, 424.3f, 653.53f, (byte) 0);
		}

		return false;
	}
}
