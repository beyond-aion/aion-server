package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;


/**
 * @author Cheatkiller
 *
 */
public class _41536AMessageInStone extends QuestHandler {
	
	private final static int questId = 41536;
	
	private final static int[] npc_ids = {205944, 701238, 701239, 701240};
	
	public _41536AMessageInStone(){
		super(questId);
	}
		@Override
		public void register() {
			qe.registerQuestNpc(205944).addOnQuestStart(questId);
			for(int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		}
		
		@Override
		public boolean onDialogEvent(QuestEnv env) {
			Player player = env.getPlayer();
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			DialogAction dialog = env.getDialog();
			int targetId = env.getTargetId();

			if (qs == null || qs.getStatus() == QuestStatus.NONE) {
				if (targetId == 205944) { 
					if (dialog == DialogAction.QUEST_SELECT) {
						return sendQuestDialog(env, 1011);
					}
					else {
						return sendQuestStartDialog(env);
					}
				}
			}
			else 	if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			 if (targetId == 701238 && var == 0) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 1352);
				}
				else if (dialog == DialogAction.SETPRO1) {
					giveQuestItem(env, 182212534, 1);
					changeQuestStep(env, 0, 1, false);
					return closeDialogWindow(env);
				}
			 }
			 if (targetId == 701239 && var == 1) {
					if (dialog == DialogAction.USE_OBJECT) {
						return sendQuestDialog(env, 1693);
					}
					else if (dialog == DialogAction.SETPRO2) {
						giveQuestItem(env, 182212535, 1);
						changeQuestStep(env, 1, 2, false);
						return closeDialogWindow(env);
					}
				 }
			 if (targetId == 701240 && var == 2) {
					if (dialog == DialogAction.USE_OBJECT) {
						return sendQuestDialog(env, 2034);
					}
					else if (dialog == DialogAction.SETPRO3) {
						giveQuestItem(env, 182212536, 1);
						changeQuestStep(env, 2, 3, false);
						return closeDialogWindow(env);
					}
				 }
			 if (targetId == 205944 && var == 3) {
					if (dialog == DialogAction.QUEST_SELECT) {
						return sendQuestDialog(env, 2375);
					}
					else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
						removeQuestItem(env, 182212534, 1);
						removeQuestItem(env, 182212535, 1);
						removeQuestItem(env, 182212536, 1);
						return defaultCloseDialog(env, 3, 3, true, true);
					}
				 }
			}
			else if (qs.getStatus() == QuestStatus.REWARD) {
				if (targetId == 205944) {
					switch (dialog) {
						case USE_OBJECT: {
							return sendQuestDialog(env, 10002);
						}
						default: {
							return sendQuestEndDialog(env);
						}
					}
				}
			}
			return false;
		}
}