package quest.altgard;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @Author Majka
 */
public class _2263ShugoPotion extends AbstractQuestHandler {

	private final static int questDropItemId = 182203242; // Malodor Pollen
	private final static int questStartNpcId = 798036; // Mabrunerk

	public _2263ShugoPotion() {
		super(2263);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(questStartNpcId).addOnQuestStart(questId); // Mabrunerk
		qe.registerQuestNpc(questStartNpcId).addOnTalkEvent(questId); // Mabrunerk
		qe.registerOnQuestTimerEnd(questId);
		qe.registerOnLogOut(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc) {
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		}
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == questStartNpcId) { // Mabrunerk
				switch (dialogActionId) {
					case QUEST_ACCEPT_1:
						if (QuestService.startQuest(env)) {
							QuestService.questTimerStart(env, 300);
							return sendQuestDialog(env, 1003);
						}
						break;
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					default:
						return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {

			if (targetId == questStartNpcId) { // Mabrunerk
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);

					case CHECK_USER_HAS_QUEST_ITEM:
						// Checks if player has 3 Malodor Pollens [ID: 182203242]
						if (QuestService.collectItemCheck(env, true)) {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							QuestService.questTimerEnd(env);
							return sendQuestDialog(env, 5);
						} else {
							return sendQuestDialog(env, 2716);
						}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == questStartNpcId) { // Mabrunerk
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	// On time end if not in reward status delete quest items and quest itself
	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			long malodorPollen = player.getInventory().getItemCountByItemId(questDropItemId);
			removeQuestItem(env, questDropItemId, malodorPollen);
			QuestService.abandonQuest(player, questId);
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
			long malodorPollen = player.getInventory().getItemCountByItemId(questDropItemId);
			removeQuestItem(env, questDropItemId, malodorPollen);
			QuestService.abandonQuest(player, questId);
			return true;
		}
		return false;
	}
}
