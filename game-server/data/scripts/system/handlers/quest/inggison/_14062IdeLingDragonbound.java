package quest.inggison;

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
public class _14062IdeLingDragonbound extends QuestHandler {
	
	private final static int questId = 14062;
	
	private final static int[] npc_ids = {799053, 799029, 798979, 798926, 799053 , 798927};
	
	private final static int[] mobs = {215662, 215667, 215661, 215666, 215664, 215660};
	
	
	public _14062IdeLingDragonbound(){
		super(questId);
	}
		@Override
		public void register() {
			qe.registerOnLevelUp(questId);
			qe.registerQuestNpc(206349).addOnAtDistanceEvent(questId);
			for(int npc_id : npc_ids)
				qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
			for(int mob : mobs)
				qe.registerQuestNpc(mob).addOnKillEvent(questId);
			
			
		}
		
		@Override
		public boolean onDialogEvent(QuestEnv env) {
			Player player = env.getPlayer();
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			DialogAction dialog = env.getDialog();
			int targetId = env.getTargetId();

			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);
				if (targetId == 799053) {
					if (dialog == DialogAction.QUEST_SELECT) {
						if (var == 0)
							return sendQuestDialog(env, 1011);
						else {
							if (var == 11) {
								return sendQuestDialog(env, 3398);
							}
						}
					}
					else if (dialog == DialogAction.SETPRO1) {
						return defaultCloseDialog(env, 0, 1);
					}
					else if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
						return checkQuestItems(env, 11, 12, true, 10000, 10001);
					}
				}
				if (targetId == 799029) {
					 if (dialog == DialogAction.QUEST_SELECT) {
						 if (var == 1)
							 return sendQuestDialog(env, 1352);
						 else if (var == 8)
							 return sendQuestDialog(env, 2034);
					 }
					 else if (dialog == DialogAction.SETPRO2) {
						 return defaultCloseDialog(env, 1, 2);
					 }
					 else if (dialog == DialogAction.SETPRO4) {
						 return defaultCloseDialog(env, 8, 9);
					 }
				 }
				 if (targetId == 798979 && var == 9) {
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 2375);
						}
						else if (dialog == DialogAction.SETPRO5) {
							return defaultCloseDialog(env, 9, 10);
						}
				 }
				 if (targetId == 798926 && var == 3) {
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 2034);
						}
						else if (dialog == DialogAction.SETPRO4) {
							return defaultCloseDialog(env, 3, 4);
						}
				 }
				 if (targetId == 799053 && var == 4) {
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 2375);
						}
						else if (dialog == DialogAction.SETPRO5) {
							return defaultCloseDialog(env, 4, 4, true, false);
						}
				 }
			}
			else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
				if (targetId == 799053) {
					if (dialog == DialogAction.USE_OBJECT) {
						return sendQuestDialog(env, 10002);
					}
					return sendQuestEndDialog(env);
				}
			}
			return false;
		}
		
		@Override
		public boolean onLvlUpEvent(QuestEnv env) {
			return defaultOnLvlUpEvent(env, 14061);
		}
		
		@Override
    public boolean onKillEvent(QuestEnv env) {
        return defaultOnKillEvent(env, mobs, 2, 8);
    }
		
		@Override
		public boolean onAtDistanceEvent(QuestEnv env) {
			Player player = env.getPlayer();
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
	  		int var = qs.getQuestVarById(0);
	  		if (var == 10) {
	  			changeQuestStep(env, 10, 11, false);
	    		return true;
	  		}
	  	}
			return false;
		}
}

