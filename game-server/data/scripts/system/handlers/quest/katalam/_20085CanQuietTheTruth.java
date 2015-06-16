package quest.katalam;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _20085CanQuietTheTruth extends QuestHandler {

	private final static int questId = 20085;
		

	public _20085CanQuietTheTruth() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcIds = { 800545, 800564, 800567};
		qe.registerQuestItem(182215240, questId);
		qe.registerQuestNpc(206285).addOnAtDistanceEvent(questId);
		qe.registerOnLevelUp(questId);
		for (int npcId : npcIds) {
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		}
	}
	
	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 20084);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 800545) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 0) {
							return sendQuestDialog(env, 1011);
						}
						else if (qs.getQuestVarById(0) == 2) {
							return sendQuestDialog(env, 1693);
						}
					}
					case SETPRO1: {
						giveQuestItem(env, 182215240, 1);
						return defaultCloseDialog(env, 0, 1);
					}
					case SETPRO3: {
						removeQuestItem(env, 182215240, 1);
						giveQuestItem(env, 182215241, 1);
						return defaultCloseDialog(env, 2, 3);
					}
				}
			}
			else if (targetId == 800564) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 3) {
							return sendQuestDialog(env, 2034);
						}
					}
					case SET_SUCCEED: {
						removeQuestItem(env, 182215241, 1);
						return defaultCloseDialog(env, 3, 4, true, false); 
					}
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800567) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getQuestVarById(0) == 1) {
			changeQuestStep(env, 1, 2, false);
		  return HandlerResult.SUCCESS;
		}
		return HandlerResult.FAILED;
	}
}
