package quest.pandaemonium;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Nephis and AU team helper
 */
public class _2911SongOfBlessing extends AbstractQuestHandler {

	public _2911SongOfBlessing() {
		super(2911);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204079).addOnQuestStart(questId);
		qe.registerQuestNpc(204079).addOnTalkEvent(questId);
		qe.registerQuestNpc(204193).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 204079) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 204193) {
			if (qs != null && qs.getStatus() != QuestStatus.COMPLETE) {
				if (env.getDialogActionId() == QUEST_SELECT && qs.getStatus() == QuestStatus.START)
					return sendQuestDialog(env, 1352);
				else if (env.getDialogActionId() == SETPRO1) {
					qs.setQuestVar(2);
					qs.setRewardGroup(0);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestDialog(env, 5);
				} else if (env.getDialogActionId() == SETPRO2) {
					qs.setQuestVar(3);
					qs.setRewardGroup(1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return sendQuestDialog(env, 6);
				} else
					return sendQuestEndDialog(env); // different rewards and follow-up quests, depending on the decision
			}
		}
		return false;
	}
}
