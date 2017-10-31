package quest.event_quests;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.quest.QuestItems;
import com.aionemu.gameserver.model.templates.rewards.BonusType;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Rolandas
 */
public class _80034EventGeaterGlories extends AbstractQuestHandler {

	public _80034EventGeaterGlories() {
		super(80034);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799765).addOnQuestStart(questId);
		qe.registerQuestNpc(799765).addOnTalkEvent(questId);
		qe.registerOnBonusApply(questId, BonusType.LUNAR);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable())
			return false;

		if (qs.getStatus() == QuestStatus.START || qs.getStatus() == QuestStatus.COMPLETE && QuestService.collectItemCheck(env, false)) {
			if (targetId == 799765) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (env.getDialogActionId() == QUEST_ACCEPT_1)
					return sendQuestDialog(env, 1003);
				else if (env.getDialogActionId() == CHECK_USER_HAS_QUEST_ITEM)
					return checkQuestItems(env, 0, 0, true, 5, 2716);
				else if (env.getDialogActionId() == SELECTED_QUEST_NOREWARD)
					return sendQuestRewardDialog(env, 799765, 5);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (env.getDialogActionId() == USE_OBJECT)
				return sendQuestDialog(env, 5);
			return sendQuestEndDialog(env);
		}
		return sendQuestRewardDialog(env, 799765, 0);
	}

	@Override
	public HandlerResult onBonusApplyEvent(QuestEnv env, BonusType bonusType, List<QuestItems> rewardItems) {
		if (bonusType != BonusType.LUNAR || env.getQuestId() != questId)
			return HandlerResult.UNKNOWN;
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && (qs.getStatus() == QuestStatus.START || qs.getStatus() == QuestStatus.COMPLETE)) {
			if (qs.getQuestVarById(0) == 0)
				return HandlerResult.SUCCESS;
		}
		return HandlerResult.FAILED;
	}
}
