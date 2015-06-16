package quest.sarpan;

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
 *
 */
public class _41222MapMania extends QuestHandler {

	private final static int questId = 41222;

	public _41222MapMania() {
		super(questId);
	}

	public void register() {
		qe.registerQuestItem(182213150, questId);
		qe.registerQuestItem(182213105, questId);
		qe.registerQuestNpc(800127).addOnTalkEvent(questId);
		qe.registerQuestNpc(730475).addOnTalkEvent(questId);
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
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 800127) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				}
				else if (dialog == DialogAction.SETPRO1) {
					removeQuestItem(env, 182213105, 1);
					giveQuestItem(env, 182213150, 1);
					giveQuestItem(env, 182213187, 1);
					return defaultCloseDialog(env, 0, 1);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 730475) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				Npc npc = (Npc) env.getVisibleObject();
				npc.getController().onDelete();
				removeQuestItem(env, 182213150, 1);
				removeQuestItem(env, 182213187, 1);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
	
									
	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if(item.getItemId() == 182213105) {
			if (qs == null || qs.getStatus() == QuestStatus.NONE) {
				return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
			}
		}
		else if(item.getItemId() == 182213150) {
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				changeQuestStep(env, 1, 1, true);
				QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 730475, player.getX(), player.getY(), player.getZ(), (byte) 0);
				return HandlerResult.SUCCESS;
			}
		}
		return HandlerResult.FAILED;
	}
}
