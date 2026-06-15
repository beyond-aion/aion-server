package quest.theobomos;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;

/**
 * @author Leunam
 */
public class _3058StoneofMabolo extends AbstractQuestHandler {

	public _3058StoneofMabolo() {
		super(3058);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798189).addOnTalkEvent(questId);
		qe.registerQuestNpc(203701).addOnTalkEvent(questId);
		qe.registerQuestNpc(798213).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (env.getDialogActionId() == ASK_QUEST_ACCEPT) {
				return sendQuestDialog(env, 4);
			} else if (env.getDialogActionId() == QUEST_ACCEPT_1) {
				return sendQuestStartDialog(env);
			} else if (env.getDialogActionId() == QUEST_REFUSE_1) {
				return closeDialogWindow(env);
			}
		}
		if (qs == null)
			return false;

		if (targetId == 798189) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					return sendQuestDialog(env, 1352);
				case SETPRO1:
					return defaultCloseDialog(env, 0, 1);
			}
		} else if (targetId == 203701) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					return sendQuestDialog(env, 1693);
				case SETPRO2:
					return defaultCloseDialog(env, 1, 2);
			}
		} else if (targetId == 798213) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					return sendQuestDialog(env, 2375);
				case SELECT_QUEST_REWARD:
					changeQuestStep(env, 2, 2, true);
			}
			return sendQuestEndDialog(env);
		}
		return false;
	}
}
