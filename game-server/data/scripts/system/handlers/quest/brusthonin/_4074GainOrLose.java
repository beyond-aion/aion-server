package quest.brusthonin;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.item.ItemService;

/**
 * @author Wakizashi
 * @modified vlog
 */
public class _4074GainOrLose extends QuestHandler {

	public _4074GainOrLose() {
		super(4074);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205181).addOnQuestStart(questId);
		qe.registerQuestNpc(205181).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.isStartable()) {
			if (targetId == 205181) { // Bonarunerk
				if (dialog == DialogAction.EXCHANGE_COIN) {
					if (QuestService.startQuest(env)) {
						return sendQuestDialog(env, 1011);
					} else {
						return sendQuestSelectionDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205181) { // Bonarunerk
				long kinahAmount = player.getInventory().getKinah();
				long demonsEye = player.getInventory().getItemCountByItemId(186000038);
				switch (dialog) {
					case EXCHANGE_COIN: {
						return sendQuestDialog(env, 1011);
					}
					case SELECT_ACTION_1011: {
						if (kinahAmount >= 1000 && demonsEye >= 1) {
							changeQuestStep(env, 0, 0, true);
							qs.setReward(0);
							return sendQuestDialog(env, 5);
						} else {
							return sendQuestDialog(env, 1009);
						}
					}
					case SELECT_ACTION_1352: {
						if (kinahAmount >= 5000 && demonsEye >= 1) {
							changeQuestStep(env, 0, 0, true);
							qs.setReward(1);
							return sendQuestDialog(env, 6);
						} else {
							return sendQuestDialog(env, 1009);
						}
					}
					case SELECT_ACTION_1693: {
						if (kinahAmount >= 25000 && demonsEye >= 1) {
							changeQuestStep(env, 0, 0, true);
							qs.setReward(2);
							return sendQuestDialog(env, 7);
						} else {
							return sendQuestDialog(env, 1009);
						}
					}
					case FINISH_DIALOG: {
						return sendQuestSelectionDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205181) { // Bonarunerk
				if (dialog == DialogAction.SELECTED_QUEST_NOREWARD) {
					if (qs.getReward() == null)
						return false;
					switch (qs.getReward()) {
						case 0:
							if (player.getInventory().tryDecreaseKinah(1000) && QuestService.finishQuest(env, 0)) {
								removeQuestItem(env, 186000038, 1);
								ItemService.addItem(player, 186000010, 1);
							}
							break;
						case 1:
							if (player.getInventory().tryDecreaseKinah(5000) && QuestService.finishQuest(env, 1)) {
								removeQuestItem(env, 186000038, 1);
								ItemService.addItem(player, 186000010, Rnd.get(1, 3));
							}
							break;
						case 2:
							if (player.getInventory().tryDecreaseKinah(25000) && QuestService.finishQuest(env, 2)) {
								removeQuestItem(env, 186000038, 1);
								ItemService.addItem(player, 186000010, Rnd.get(1, 6));
							}
							break;
					}
					return closeDialogWindow(env);
				} else {
					QuestService.abandonQuest(player, questId);
				}
			}
		}
		return false;
	}
}
