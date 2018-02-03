package quest.event_quests;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.event.EventService;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rolandas
 */
public class _80033EventAvertingTheGaze extends AbstractQuestHandler {

	public _80033EventAvertingTheGaze() {
		super(80033);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799781).addOnQuestStart(questId);
		qe.registerQuestNpc(799781).addOnTalkEvent(questId);
		qe.registerQuestItem(188051133, questId); // [Event] Charm Card
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable())
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799781) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (env.getDialogActionId() == QUEST_ACCEPT_1) {
					Storage storage = player.getInventory();
					if (storage.getItemCountByItemId(164002015) > 0)
						return sendQuestDialog(env, 2375);
					else
						return sendQuestDialog(env, 2716);
				} else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
					if (qs.getQuestVarById(0) == 0)
						defaultCloseDialog(env, 0, 1, true, true, 0, 0, 164002015, 1);
					return sendQuestDialog(env, 5);
				} else if (env.getDialogActionId() == SELECTED_QUEST_NOREWARD)
					return sendQuestRewardDialog(env, 799781, 5);
				else
					return sendQuestStartDialog(env);
			}
		}
		return sendQuestRewardDialog(env, 799781, 0);
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		// check if the parent quest is active (you get Charm Cards)
		if (!EventService.getInstance().isActiveEventQuest(80032))
			return HandlerResult.FAILED;

		final Player player = env.getPlayer();

		// the same item registered for elyos quests, return UNKNOWN for them
		// to not start asmodians quests
		if (item.getItemId() == 188051133 && player.getCommonData().getRace().equals(Race.ASMODIANS)) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					Storage storage = player.getInventory();
					QuestStatus status;

					if (storage.getItemCountByItemId(164002015) > 0) {
						status = getQuestUpdateStatus(player, questId);
						// got a Beritra's Gaze, then start me
						QuestService.startEventQuest(new QuestEnv(null, player, questId), status);
					}
					if (storage.getItemCountByItemId(164002016) > 9) { // Israphel's Glory
						status = getQuestUpdateStatus(player, 80037);
						QuestService.startEventQuest(new QuestEnv(null, player, 80037), status);
					}
					if (storage.getItemCountByItemId(164002017) > 4) { // Siel's Gift
						status = getQuestUpdateStatus(player, 80038);
						QuestService.startEventQuest(new QuestEnv(null, player, 80038), status);
					}
					if (storage.getItemCountByItemId(164002018) > 0) { // Aion's Grace
						status = getQuestUpdateStatus(player, 80039);
						QuestService.startEventQuest(new QuestEnv(null, player, 80039), status);
					}
				}
			}, 10000);
			return HandlerResult.SUCCESS;
		}
		return HandlerResult.UNKNOWN;
	}

	final QuestStatus getQuestUpdateStatus(Player player, int questid) {
		QuestState qs = player.getQuestStateList().getQuestState(questid);
		QuestStatus status = qs == null ? QuestStatus.START : qs.getStatus();

		if (qs != null && questid != questId && status == QuestStatus.COMPLETE)
			status = QuestStatus.START;
		return status;
	}

}
