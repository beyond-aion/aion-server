package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author mr.madison
 */
public class _41504Groundbreaking extends QuestHandler {

	private final static int questId = 41504;

	public _41504Groundbreaking() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205935).addOnQuestStart(questId);
		qe.registerQuestNpc(205935).addOnTalkEvent(questId);
		qe.registerQuestNpc(205891).addOnTalkEvent(questId);
		qe.registerQuestNpc(205887).addOnTalkEvent(questId);
		qe.registerQuestItem(182212517, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205935) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 4762);
					}
					case QUEST_ACCEPT_SIMPLE: {
						return sendQuestStartDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205891) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 1011);
					}
					case CHECK_USER_HAS_QUEST_ITEM: {
						if (QuestService.collectItemCheck(env, true)) {
							giveQuestItem(env, 182212517, 1);
							changeQuestStep(env, 0, 1, false);
							updateQuestStatus(env);
							return sendQuestDialog(env, 10000);
						} else
							return sendQuestDialog(env, 10001);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205887) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.isInsideZone(ZoneName.get("GIANT_CRATER_600030000")) && item.getItemId() == 182212517) {
				changeQuestStep(env, 1, 2, true);
			}
		}
		return HandlerResult.FAILED;
	}
}
