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

/**
 * @author Artur
 */
public class _80145EventWarpedRiftSealing extends AbstractQuestHandler {

	public _80145EventWarpedRiftSealing() {
		super(80145);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(830246).addOnTalkEvent(questId);
		qe.registerOnBonusApply(questId, BonusType.RIFT);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();

		if (env.getTargetId() == 0)
			return sendQuestStartDialog(env);

		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == 830246) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 2375);
						return false;
					case SELECT_QUEST_REWARD:
						removeQuestItem(env, 182215137, 1);
						return defaultCloseDialog(env, 0, 1, true, true);
				}
			}
		}
		return sendQuestRewardDialog(env, 830246, 0);
	}

	@Override
	public HandlerResult onBonusApplyEvent(QuestEnv env, BonusType bonusType, List<QuestItems> rewardItems) {
		if (bonusType != BonusType.RIFT || env.getQuestId() != questId)
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
