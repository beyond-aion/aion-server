package quest.beshmundir;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi
 */
public class _30302MysteriousMotives extends AbstractQuestHandler {

	public _30302MysteriousMotives() {
		super(30302);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799225).addOnQuestStart(questId);
		qe.registerQuestNpc(799225).addOnTalkEvent(questId);
		qe.registerQuestNpc(799240).addOnTalkEvent(questId);
		qe.registerQuestNpc(799243).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (targetId == 799225) {
			if (qs == null) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (env.getDialogActionId() == QUEST_ACCEPT_1)
					return sendQuestStartDialog(env);
			} else if (qs.getStatus() == QuestStatus.START) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
					changeQuestStep(env, 2, 3, true);
					return sendQuestEndDialog(env);
				}
			} else if (qs.getStatus() == QuestStatus.REWARD) {
				return sendQuestEndDialog(env);
			}
		} else if (targetId == 799240) {
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (env.getDialogActionId() == SETPRO1) {
					changeQuestStep(env, 0, 1);
					return closeDialogWindow(env);
				}
			}
		} else if (targetId == 799243) {
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (env.getDialogActionId() == SETPRO2) {
					changeQuestStep(env, 1, 2);
					return closeDialogWindow(env);
				}
			}
		}
		return super.onDialogEvent(env);
	}
}
