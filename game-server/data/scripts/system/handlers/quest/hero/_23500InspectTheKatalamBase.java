package quest.hero;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Whoop
 */
public class _23500InspectTheKatalamBase extends QuestHandler {
	
	public static final int questId = 23500;

  public _23500InspectTheKatalamBase() {
      super(questId);
  }
  
  @Override
  public void register() {
      qe.registerQuestItem(182215271, questId);
      qe.registerQuestNpc(800529).addOnQuestStart(questId); //Vard.
      qe.registerQuestNpc(801239).addOnTalkEvent(questId); //Arfast.
      qe.registerQuestNpc(801241).addOnTalkEvent(questId); //Cavloft.
      qe.registerQuestNpc(801244).addOnTalkEvent(questId); //Fjotra.
  }
  
  @Override
  public boolean onDialogEvent(QuestEnv env) {
  	Player player = env.getPlayer();
  	int targetId = env.getTargetId();
    QuestState qs = player.getQuestStateList().getQuestState(questId);
    DialogAction dialog = env.getDialog();
    
    if (qs == null || qs.getStatus() == QuestStatus.NONE) {
    	if (targetId == 800529) {
    		switch (dialog) {
    			case QUEST_SELECT:
    				return sendQuestDialog(env, 1011);
    			case QUEST_ACCEPT_SIMPLE:
    				return sendQuestDialog(env, 1012);
    			case QUEST_ACCEPT:
    				return sendQuestDialog(env, 1013);
    			case SETPRO1:
    				QuestService.startQuest(env);
    				changeQuestStep(env, 0, 1, false);
    				giveQuestItem(env, 182215271, 1);
    				return sendQuestDialog(env, 1352);    				
    			case SETPRO2:
    				QuestService.startQuest(env);
    				changeQuestStep(env, 0, 2, false);
    				giveQuestItem(env, 182215271, 1);
    				return sendQuestDialog(env, 1693);
    			case SETPRO3:
    				QuestService.startQuest(env);
    				changeQuestStep(env, 0, 3, false);
    				giveQuestItem(env, 182215271, 1);
    				return sendQuestDialog(env, 2034);
    		}
    	}
    } else if (qs.getStatus() == QuestStatus.START) {
    	switch (targetId) {
    		case 801239:
    			switch (dialog) {
    				case QUEST_SELECT:
    					return sendQuestDialog(env, 2375);
    				case SELECT_QUEST_REWARD:
    					removeQuestItem(env, 182215271, 1);
    					return defaultCloseDialog(env, 1, 4, true, true, 0);
    			}
    		case 801241:
    			switch (dialog) {
    				case QUEST_SELECT:
    					return sendQuestDialog(env, 2716);
    				case SELECT_QUEST_REWARD:
    					removeQuestItem(env, 182215271, 1);
    					return defaultCloseDialog(env, 2, 5, true, true, 1);
    			}
    		case 801244:
    			switch (dialog) {
    				case QUEST_SELECT:
    					return sendQuestDialog(env, 3057);
    				case SELECT_QUEST_REWARD:
    					removeQuestItem(env, 182215271, 1);
    					return defaultCloseDialog(env, 3, 6, true, true, 2);
    			}
    	}
    } else if (qs.getStatus() == QuestStatus.REWARD) {
    	switch (targetId) {
    		case 801239:
                return sendQuestEndDialog(env, 0);
            case 801241:
                return sendQuestEndDialog(env, 1);
            case 801244:
                return sendQuestEndDialog(env, 2);
        }
    }
  	return false;
  }
}
