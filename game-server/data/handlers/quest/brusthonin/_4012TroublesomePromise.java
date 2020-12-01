package quest.brusthonin;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author VladimirZ
 */
public class _4012TroublesomePromise extends AbstractQuestHandler {

	public _4012TroublesomePromise() {
		super(4012);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205142).addOnQuestStart(questId);
		qe.registerQuestNpc(205142).addOnTalkEvent(questId);
		qe.registerQuestNpc(730104).addOnTalkEvent(questId);
		qe.registerQuestNpc(700342).addOnTalkEvent(questId);
		qe.addHandlerSideQuestDrop(questId, 700342, 182209005, 1, 100);
		qe.registerOnGetItem(182209005, questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (targetId == 205142) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 700342:
					if (qs.getQuestVarById(0) == 0 && env.getDialogActionId() == USE_OBJECT) {
						return true; // loot
					}
					break;
				case 730104:
					if (qs.getQuestVarById(0) == 1) {
						if (env.getDialogActionId() == QUEST_SELECT)
							return sendQuestDialog(env, 2375);
						else if (env.getDialogActionId() == CHECK_USER_HAS_QUEST_ITEM) {
							removeQuestItem(env, 182209005, 1);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestDialog(env, 5);
						} else
							return sendQuestEndDialog(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 730104)
				return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public boolean onGetItemEvent(QuestEnv env) {
		return defaultOnGetItemEvent(env, 0, 0, true); // reward
	}
}
