package quest.event_quests;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.List;

import com.aionemu.commons.utils.Rnd;
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
public class _80018EventSockItToEm extends AbstractQuestHandler {

	public _80018EventSockItToEm() {
		super(80018);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799778).addOnQuestStart(questId);
		qe.registerQuestNpc(799778).addOnTalkEvent(questId);
		qe.registerOnBonusApply(questId, BonusType.MOVIE);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (env.getTargetId() == 799778) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_1:
						QuestService.startEventQuest(env, QuestStatus.START);
						return sendQuestDialog(env, 1003);
					default:
						return sendQuestStartDialog(env);
				}
			}
			return false;
		}

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == 799778) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 2375);
						return false;
					case CHECK_USER_HAS_QUEST_ITEM:
						return checkQuestItems(env, 0, 1, true, 5, 2716);
				}
			}
		}

		return sendQuestRewardDialog(env, 799778, 0);
	}

	@Override
	public HandlerResult onBonusApplyEvent(QuestEnv env, BonusType bonusType, List<QuestItems> rewardItems) {
		if (bonusType != BonusType.MOVIE || env.getQuestId() != questId)
			return HandlerResult.UNKNOWN;

		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (qs.getCompleteCount() == 9) { // [Event] Hat Box
				rewardItems.add(new QuestItems(188051106, 1));
			}
			// randomize movie
			playQuestMovie(env, Rnd.nextBoolean() ? 135 : 136);
			return HandlerResult.SUCCESS;
		}
		return HandlerResult.FAILED;
	}
}
