package quest.altgard;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Mr. Poke
 */
public class _2209TheScribbler extends AbstractQuestHandler {

	public _2209TheScribbler() {
		super(2209);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203555).addOnQuestStart(questId);
		qe.registerQuestNpc(203555).addOnTalkEvent(questId);
		qe.registerQuestNpc(203562).addOnTalkEvent(questId);
		qe.registerQuestNpc(203592).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			if (targetId == 203555) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203562:
					if (qs.getQuestVarById(0) == 0) {
						if (env.getDialogActionId() == QUEST_SELECT)
							return sendQuestDialog(env, 1352);
						else if (env.getDialogActionId() == SETPRO1) {
							return defaultCloseDialog(env, 0, 1); // 1
						}
					}
					break;
				case 203572:
					if (qs.getQuestVarById(0) == 1) {
						if (env.getDialogActionId() == QUEST_SELECT)
							return sendQuestDialog(env, 1693);
						else if (env.getDialogActionId() == SETPRO2) {
							return defaultCloseDialog(env, 1, 2); // 2
						}
					}
					break;
				case 203592:
					if (qs.getQuestVarById(0) == 2) {
						if (env.getDialogActionId() == QUEST_SELECT)
							return sendQuestDialog(env, 2034);
						else if (env.getDialogActionId() == SETPRO3) {
							return defaultCloseDialog(env, 2, 3); // 3
						}
					}
					break;
				case 203555:
					if (qs.getQuestVarById(0) == 3) {
						if (env.getDialogActionId() == QUEST_SELECT)
							return sendQuestDialog(env, 2375);
						else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestEndDialog(env);
						} else
							return sendQuestEndDialog(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203555)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
