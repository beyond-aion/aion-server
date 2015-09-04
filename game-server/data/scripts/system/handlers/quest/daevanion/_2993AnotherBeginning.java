package quest.daevanion;

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
public class _2993AnotherBeginning extends QuestHandler {

	private static final int questId = 2993;
	private final static int dialogs[] = { 1013, 1034, 1055, 1076, 5103, 1098, 1119, 1140, 1161, 5104, 1183, 1204, 1225, 1246, 5105, 1268,
		1289, 1310, 1331, 5106, 2376, 2461, 2546, 2631, 2632 };
	private final static int items[] = { 110600834, 113600800, 114600794, 112600785, 111600813, 110300881, 113300860, 114300893, 112300784,
		111300834, 110100931, 113100843, 114100866, 112100790, 111100831, 110500849, 113500827, 114500837, 112500774, 111500821, 110301532,
		113301497, 114301526, 112301412, 111301470 };

	public _2993AnotherBeginning() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204076).addOnQuestStart(questId);
		qe.registerQuestNpc(204076).addOnTalkEvent(questId);
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
			if (targetId == 204076) { // Narvi
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
			if (targetId == 204076) { // Narvi
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
					switch (dialogId) {
						case 1012:
						case 1097:
						case 1182:
						case 1267:
						case 2375:
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
							if (player.getInventory().getItemCountByItemId(186000041) == 0) // Daevanion's Light
								return sendQuestDialog(env, 1009);
							changeQuestStep(env, 0, 0, true);
							qs.setReward(savedData | (dialogId - 10000));
							return sendQuestDialog(env, dialogId - 10000 + 41);
					}
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204076) { // Narvi
				int savedData = qs.getReward();
				if (removeQuestItem(env, items[savedData >> 4], 1) && removeQuestItem(env, 186000041, 1)) {
					return sendQuestEndDialog(env, savedData & 0x7);
				}
			}
		}
		return false;
	}
}
