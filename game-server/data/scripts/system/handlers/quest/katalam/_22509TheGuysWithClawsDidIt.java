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
 * 
 * @author Cheatkiller
 */
public class _22509TheGuysWithClawsDidIt extends QuestHandler {

	private static final int questId = 22509;

	public _22509TheGuysWithClawsDidIt() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(800989).addOnQuestStart(questId);
		qe.registerQuestNpc(801007).addOnTalkEvent(questId);
		qe.registerQuestNpc(801003).addOnTalkEvent(questId);
		qe.registerQuestNpc(701743).addOnKillEvent(questId);
		qe.registerQuestItem(182213338, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 800989) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 4762);
					}
					default: {
						return sendQuestStartDialog(env, 182213338, 1);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801003) {
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
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 701743, 0, 1);
	}
	
	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {
			changeQuestStep(env, 1, 1, true);
			return HandlerResult.fromBoolean(removeQuestItem(env, 182213338, 1));
		}
		return HandlerResult.FAILED;
	}
}