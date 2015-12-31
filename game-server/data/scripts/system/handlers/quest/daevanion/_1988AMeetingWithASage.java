package quest.daevanion;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Nephis
 * @modified Pad
 */
public class _1988AMeetingWithASage extends QuestHandler {

	private final static int questId = 1988;

	public _1988AMeetingWithASage() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203725).addOnQuestStart(questId);// Leah
		qe.registerQuestNpc(203725).addOnTalkEvent(questId);// Leah
		qe.registerQuestNpc(203989).addOnTalkEvent(questId);// Tumblusen
		qe.registerQuestNpc(798018).addOnTalkEvent(questId);// Paorunerk
		qe.registerQuestNpc(203771).addOnTalkEvent(questId);// Fermina
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 203725) { // Leah
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 203989) { // Tumblusen
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (env.getDialog() == DialogAction.SETPRO1)
					return defaultCloseDialog(env, 0, 1);
			}
		} else if (targetId == 798018) { // Paorunerk
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (env.getDialog() == DialogAction.SETPRO2)
					return defaultCloseDialog(env, 1, 2);
			}
		} else if (targetId == 203771) { // Fermina
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 2) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 2034);
				else if (env.getDialog() == DialogAction.SELECT_ACTION_2035)
					return checkQuestItems(env, 2, 2, true, 2035, 2120);
			} else if (qs.getStatus() == QuestStatus.REWARD) {
				return sendQuestEndDialog(env);
			}
		}

		return false;
	}
}
