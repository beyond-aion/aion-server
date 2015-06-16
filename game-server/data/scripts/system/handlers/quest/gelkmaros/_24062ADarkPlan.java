package quest.gelkmaros;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;


/**
 * @author Ritsu
 *
 */
public class _24062ADarkPlan extends QuestHandler {
	
	private final static int questId = 24062;
	
	private final static int[] npc_ids = {799364, 799295, 799326};
	
	private final static int[] mobs = {216055, 216056, 216057, 216058, 216059, 216060, 216061, 216062, 216063, 216064, 216065, 216066, 216067, 216068, 216069, 216070	};
	
	public _24062ADarkPlan(){
		super(questId);
	}
		@Override
		public void register() {
			qe.registerOnLevelUp(questId);
			qe.registerQuestNpc(206350).addOnAtDistanceEvent(questId);
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
				if (targetId == 799364) {
					if (dialog == DialogAction.QUEST_SELECT) {
						if (var == 0)
							return sendQuestDialog(env, 1011);
						if (var == 11)
							return sendQuestDialog(env, 4166);
					}
					else if (dialog == DialogAction.SETPRO1) {
						return defaultCloseDialog(env, 0, 1);
					}
					else if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
						return checkQuestItems(env, 11, 11, true, 10000, 10001);
					}
				}
				if (targetId == 799295) {
					 if (dialog == DialogAction.QUEST_SELECT) {
						 if (var == 1)
							 return sendQuestDialog(env, 1352);
						 else if (var == 8)
							 return sendQuestDialog(env, 3739);
					 }
					 else if (dialog == DialogAction.SETPRO2) {
						 return defaultCloseDialog(env, 1, 2);
					 }
					 else if (dialog == DialogAction.SETPRO9) {
						 return defaultCloseDialog(env, 8, 9);
					 }
					 else if (dialog == DialogAction.SELECT_ACTION_3740) {
						 return sendQuestDialog(env, 3740);
					 }
				 }
				 if (targetId == 799326 && var == 9) {
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 4080);
						}
						else if (dialog == DialogAction.SETPRO10) {
							return defaultCloseDialog(env, 9, 10);
						}
				 }
			}
			else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
				if (targetId == 799364) {
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
			return defaultOnLvlUpEvent(env, 24061);
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

