package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;


/**
 * @author Cheatkiller
 *
 */
public class _41545UnwittingSubjects extends QuestHandler {

	private final static int questId = 41545;

	public _41545UnwittingSubjects() {
		super(questId);
	}

	public void register() {
		qe.registerQuestSkill(10380, questId);
		qe.registerQuestNpc(205969).addOnQuestStart(questId);
		qe.registerQuestNpc(205969).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205969) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else if(dialog == DialogAction.QUEST_ACCEPT_SIMPLE) {
					giveQuestItem(env, 182212543, 1);
					return sendQuestStartDialog(env);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205969) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 10002);
					}
					default: {
						removeQuestItem(env, 182212543, 1);
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onUseSkillEvent(QuestEnv env, int skillUsedId) {
		Player player = env.getPlayer();
		Npc npc = (Npc) player.getTarget();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START && skillUsedId == 10380) {
			int var = qs.getQuestVarById(0);
			if (npc == null || var > 2)
				return false;
			if(player.getTarget() == npc && npc.getName().startsWith("gurriki")) {
				if (var < 2)
					changeQuestStep(env, var, var + 1, false);
					else
						changeQuestStep(env, 2, 2, true);
				return true;
			}
		}
		return false;
	}
}