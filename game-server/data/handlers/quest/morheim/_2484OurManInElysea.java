package quest.morheim;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Mr.Poke, Nephis and quest helper team
 */
public class _2484OurManInElysea extends AbstractQuestHandler {

	public _2484OurManInElysea() {
		super(2484);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204407).addOnQuestStart(questId);
		qe.registerQuestNpc(204407).addOnTalkEvent(questId);
		qe.registerQuestNpc(700267).addOnTalkEvent(questId);
		qe.registerQuestNpc(203331).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (targetId == 204407) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else if (env.getDialogActionId() == QUEST_ACCEPT_1) {
					if (giveQuestItem(env, 182204205, 1))
						return sendQuestStartDialog(env);
					else
						return true;
				} else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 700267:
					if (qs.getQuestVarById(0) == 0 && env.getDialogActionId() == USE_OBJECT) {
						qs.setQuestVarById(0, 1);
						updateQuestStatus(env);
						removeQuestItem(env, 182204205, 1);
					}
					return false;
				case 203331:
					if (qs.getQuestVarById(0) == 1) {
						if (env.getDialogActionId() == SELECTED_QUEST_NOREWARD)
							return sendQuestDialog(env, 5);
						else if (env.getDialogActionId() == QUEST_SELECT) {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestDialog(env, 5);
						} else
							return sendQuestEndDialog(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203331)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
