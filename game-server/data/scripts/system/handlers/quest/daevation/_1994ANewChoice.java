package quest.daevation;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Tiger edited by Wakizashi, fixed Rolandas
 */
public class _1994ANewChoice extends QuestHandler {

	private final static int questId = 1994;
	private final static int dialogs[] = { 1013, 1034, 1055, 1076, 5103, 1098, 1119, 1140, 1161, 1183, 1204, 1225, 1246 };
	private final static int items[] = { 100000723, 100900554, 101300538, 100200673, 101700594, 100100568, 101500566, 100600608, 100500572,
		115000826, 101800569, 101900562, 102000592 };

	public _1994ANewChoice() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203754).addOnQuestStart(questId); // Aithra
		qe.registerQuestNpc(203754).addOnTalkEvent(questId); // Aithra
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogId = env.getDialogId();
		int dialogIndex = 0;
		boolean itemSelected = false;

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 203754) { // Aithra
				if (dialogId == DialogAction.EXCHANGE_COIN.id()) {
					QuestService.startQuest(env);
					return sendQuestDialog(env, 1011);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 203754) { // Aithra
				if (dialogId == DialogAction.EXCHANGE_COIN.id()) {
					return sendQuestDialog(env, 1011);
				}
				for (; dialogIndex < dialogs.length; dialogIndex++) {
					if (dialogs[dialogIndex] == env.getDialogId()) {
						itemSelected = true;
						break;
					}
				}
				if (itemSelected) {
					long itemCount = player.getInventory().getItemCountByItemId(items[dialogIndex]);
					if (itemCount > 0) {
						qs.setReward(dialogIndex << 4);
						return sendQuestDialog(env, 1013);
					}
					else {
						return sendQuestDialog(env, 1352);
					}
				}
				else {
					int savedData = qs.getReward();
					switch (env.getDialogId()) {
						case 1012:
						case 1097:
						case 1182:
						case 1267:
							return sendQuestDialog(env, dialogId);
						case 10000:
						case 10001:
						case 10002:
						case 10003: {
							if (player.getInventory().getItemCountByItemId(186000041) == 0) // Daevanion's Light
								return sendQuestDialog(env, 1009);
							changeQuestStep(env, 0, 0, true);
							qs.setReward(savedData | (dialogId - 10000));
							return sendQuestDialog(env, dialogId - 10000 + 5);
						}
						case 10004:
						case 10005: {
							if (player.getInventory().getItemCountByItemId(186000041) == 0) // Daevanion's Light
								return sendQuestDialog(env, 1009);
							changeQuestStep(env, 0, 0, true);
							qs.setReward(savedData | (dialogId - 10000));
							return sendQuestDialog(env, dialogId - 10000 + 41);
						}
					}
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203754) { // Aithray
				int savedData = qs.getReward();
				if (removeQuestItem(env, items[savedData >> 4], 1) && removeQuestItem(env, 186000041, 1)) {
					return sendQuestEndDialog(env, savedData & 0x7);
				}
			}
		}
		return false;
	}
}
