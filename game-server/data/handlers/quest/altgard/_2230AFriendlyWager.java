package quest.altgard;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author HellBoy, Majka
 */
public class _2230AFriendlyWager extends AbstractQuestHandler {

	private final static int questDropItemId = 182203223; // Mosbear Tusks
	private final static int questStartNpcId = 203621; // Shania
	private final static int questDurationTime = 1800; // Duration time of the quest 1800

	public _2230AFriendlyWager() {
		super(2230);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(questStartNpcId).addOnQuestStart(questId);
		qe.registerQuestNpc(questStartNpcId).addOnTalkEvent(questId);
		qe.registerOnQuestTimerEnd(questId);
		qe.registerOnLogOut(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();

		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (targetId == questStartNpcId) {
			if (qs == null || qs.isStartable()) {

				switch (dialogActionId) {
					case QUEST_ACCEPT_1:
						if (QuestService.startQuest(env)) {
							QuestService.questTimerStart(env, questDurationTime);
							return sendQuestDialog(env, 1003);
						}
						break;
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					default:
						return sendQuestStartDialog(env);
				}
			} else if (qs.getStatus() == QuestStatus.START) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					case SETPRO1:
						if (!player.getController().hasTask(TaskId.QUEST_TIMER)) { // A new chance starts
							QuestService.questTimerStart(env, questDurationTime);
						}
						return sendQuestSelectionDialog(env);
					case CHECK_USER_HAS_QUEST_ITEM:
						// Still time left; check collected items: reward if right number otherwise dialogue to continue
						if (player.getController().hasTask(TaskId.QUEST_TIMER)) {
							if (QuestService.collectItemCheck(env, true)) {
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								QuestService.questTimerEnd(env);
								return sendQuestDialog(env, 5);
							} else {
								return sendQuestDialog(env, 2716);
							}
						} else { // Time ended; remove quest items and ask for new chance;
							long mosbearTusks = player.getInventory().getItemCountByItemId(questDropItemId);
							removeQuestItem(env, questDropItemId, mosbearTusks);
							return sendQuestDialog(env, 3057);
						}
				}
			} else if (qs.getStatus() == QuestStatus.REWARD) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	// On time end if not in reward status delete timer task
	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			player.getController().cancelTask(TaskId.QUEST_TIMER);
			return true;
		}
		return false;
	}

	// On logout if not in reward status delete quest items and quest itself
	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			long mosbearTusks = player.getInventory().getItemCountByItemId(questDropItemId);
			removeQuestItem(env, questDropItemId, mosbearTusks);
			QuestService.abandonQuest(player, questId);
			return true;
		}
		return false;
	}
}
