package quest.sarpan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Cheatkiller
 */

public class _41162Bait_and_Squish extends QuestHandler {

	private final static int questId = 41162;

	public _41162Bait_and_Squish() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205567).addOnQuestStart(questId);
		qe.registerQuestNpc(730470).addOnTalkEvent(questId);
		qe.registerQuestNpc(205583).addOnTalkEvent(questId);
		qe.registerQuestNpc(218652).addOnKillEvent(questId);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 218652, 1, true);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205567) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env, 182213212, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 730470) {
				if (dialog == DialogAction.USE_OBJECT) {
					if (qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1011);
				} else if (dialog == DialogAction.SETPRO1) {
					Npc npc = (Npc) env.getVisibleObject();
					QuestService.addNewSpawn(npc.getWorldId(), npc.getInstanceId(), 218652, npc.getX(), npc.getY(), npc.getZ(), (byte) 0);
					return defaultCloseDialog(env, 0, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205583) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				removeQuestItem(env, 182213212, 1);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
