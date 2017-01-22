package quest.theobomos;

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
public class _3082DousingTheFlame extends QuestHandler {

	private final static int questId = 3082;

	public _3082DousingTheFlame() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798116).addOnQuestStart(questId);
		qe.registerQuestNpc(204030).addOnTalkEvent(questId);
		qe.registerQuestNpc(700416).addOnTalkEvent(questId);
		qe.registerQuestNpc(798155).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798116) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 204030) {
				if (dialog == DialogAction.QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0) {
						return sendQuestDialog(env, 1011);
					} else if (qs.getQuestVarById(0) == 1) {
						return sendQuestDialog(env, 1352);
					}
				} else if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
					return checkQuestItems(env, 0, 1, false, 10000, 10001, 182208060, 1);
				} else if (dialog == DialogAction.SETPRO2) {
					return defaultCloseDialog(env, 1, 2);
				}
			} else if (targetId == 700416 && qs.getQuestVarById(0) == 2) {
				Npc npc = (Npc) env.getVisibleObject();
				QuestService.addNewSpawn(npc.getWorldId(), npc.getInstanceId(), 700417, npc.getX(), npc.getY(), npc.getZ(), (byte) 0);
				removeQuestItem(env, 182208060, 1);
				return useQuestObject(env, 2, 2, true, false);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798155) {
				switch (dialog) {
					case USE_OBJECT:
						return sendQuestDialog(env, 10002);
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}
}
