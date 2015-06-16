package quest.sarpan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Cheatkiller
 *
 */
 
public class _41204BreakItTwiceShameOnMe extends QuestHandler {

	private final static int	questId	= 41204;

	public _41204BreakItTwiceShameOnMe() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205785).addOnQuestStart(questId);
		qe.registerQuestNpc(205785).addOnTalkEvent(questId);
		qe.registerQuestNpc(701161).addOnTalkEvent(questId);
		qe.registerQuestNpc(218658).addOnKillEvent(questId);
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 218658, 1, true);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205785) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 701161) {
				if (dialog == DialogAction.USE_OBJECT) {
					if(qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1011);
				}
				else if (dialog == DialogAction.SETPRO1) {
					QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 218658, player.getX(), player.getY(), player.getZ(), (byte) 0);
					return defaultCloseDialog(env, 0, 1);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205785) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
