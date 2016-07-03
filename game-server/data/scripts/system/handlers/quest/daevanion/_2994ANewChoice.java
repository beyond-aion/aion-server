package quest.daevanion;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Tiger
 * @modified Wakizashi, Rolandas, Pad, Neon
 */
public class _2994ANewChoice extends QuestHandler {

	private final static int questId = 2994;
	private final static int dialogs[] = { 1013, 1034, 1055, 1076, 5103, 1098, 1119, 1140, 1161, 1204, 1225, 1246, 5105, 1183 };
	private final static int items[][] = {
		{ 100000723, 100000724 }, { 100900554, 100900555 }, { 101300538, 101300539 }, { 100200673, 100200674 }, { 101700594, 101700595 }, 	// physical
		{ 100100568, 100100569 }, { 101500566, 101500567 }, { 100600608, 100600609 }, { 100500572, 100500573 }, { 101800569, 101800570 }, 	// magical
		{ 101900562, 101900563 }, { 102000592, 102000593 }, { 102100517, 102100518}, 
		{ 115000826, 115000828 }};																																																					// shield

	public _2994ANewChoice() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204077).addOnQuestStart(questId); // Bor
		qe.registerQuestNpc(204077).addOnTalkEvent(questId); // Bor
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		if (env.getTargetId() != 204077) // Bor
			return false;
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogId = env.getDialogId();

		if (qs == null || qs.isStartable()) {
			if (dialogId == DialogAction.EXCHANGE_COIN.id()) {
				QuestService.startQuest(env);
				return sendQuestDialog(env, 1011);
			} else {
				return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (dialogId == DialogAction.EXCHANGE_COIN.id())
				return sendQuestDialog(env, 1011);

			for (int dialogIndex = 0; dialogIndex < dialogs.length; dialogIndex++) {
				if (dialogs[dialogIndex] == dialogId) {
					for (int itemId : items[dialogIndex]) {
						if (player.getInventory().getItemCountByItemId(itemId) > 0) {
							qs.setReward(dialogIndex);
							return sendQuestDialog(env, 1013);
						}
					}
					return sendQuestDialog(env, 1352);
				}
			}
			switch (dialogId) {
				case 1012:
				case 1097:
				case 1182:
				case 1267:
					return sendQuestDialog(env, dialogId);
				case 10000:
				case 10001:
				case 10002:
				case 10003:
				case 10004:
				case 10005:
					if (player.getInventory().getItemCountByItemId(186000041) == 0) // Daevanion's Light
						return sendQuestDialog(env, 1009);
					Integer savedData = qs.getReward();
					int itemIdToRemove = 0;
					for (int itemId : items[savedData]) {
						if (player.getInventory().getItemCountByItemId(itemId) > 0)
							itemIdToRemove = itemId;
					}
					if (itemIdToRemove == 0)
						return sendQuestDialog(env, 1352);
					changeQuestStep(env, 0, 0, true);
					removeQuestItem(env, 186000041, 1);
					removeQuestItem(env, itemIdToRemove, 1);
					qs.setReward(dialogId - 10000);
					return sendQuestDialog(env, DialogPage.getRewardPageByIndex(qs.getReward()).id());
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env, qs.getReward());
		}
		return false;
	}
}
