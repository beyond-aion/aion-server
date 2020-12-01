package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Balthazar
 */
public class _1464AGiftofLove extends AbstractQuestHandler {

	public _1464AGiftofLove() {
		super(1464);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204424).addOnQuestStart(questId);
		qe.registerQuestNpc(204424).addOnTalkEvent(questId);
		qe.registerQuestNpc(203755).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204424) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204424:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							long itemCount1 = player.getInventory().getItemCountByItemId(152000455);
							if (qs.getQuestVarById(0) == 0 && itemCount1 >= 15) {
								qs.setQuestVar(0);
								qs.setStatus(QuestStatus.REWARD);
								removeQuestItem(env, 152000455, itemCount1);
								updateQuestStatus(env);
								return sendQuestDialog(env, 10000);
							} else
								return sendQuestDialog(env, 10001);
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203755) {
				if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
