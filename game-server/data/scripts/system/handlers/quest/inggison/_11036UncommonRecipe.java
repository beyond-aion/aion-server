package quest.inggison;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Cheatkiller
 */
public class _11036UncommonRecipe extends QuestHandler {

	private final static int questId = 11036;

	public _11036UncommonRecipe() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182206731, questId);
		qe.registerQuestNpc(798955).addOnTalkEvent(questId);
		qe.registerQuestNpc(798956).addOnTalkEvent(questId);
		qe.registerQuestNpc(700610).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 0) {
				if (dialog == DialogAction.QUEST_ACCEPT_1) {
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				}
			} else if (targetId == 700610) {
				Npc npc = (Npc) env.getVisibleObject();
				giveQuestItem(env, 182206731, 1);
				npc.getController().scheduleRespawn();
				npc.getController().onDelete();
				return true;
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798955) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else if (dialog == DialogAction.SETPRO1) {
					qs.setQuestVar(1);
					return defaultCloseDialog(env, 1, 1, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798956) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				}
				removeQuestItem(env, 182206731, 1);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
		}
		return HandlerResult.FAILED;
	}
}
