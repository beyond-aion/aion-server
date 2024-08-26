package quest.sanctum;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;

import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

public class _3908ToMastertheDragon extends AbstractQuestHandler {

	private final static int questId = 3908;

	public _3908ToMastertheDragon() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798316).addOnQuestStart(questId);
		qe.registerQuestNpc(798316).addOnTalkEvent(questId);
		qe.registerQuestNpc(700515).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798316) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env, 182206056, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 700515: {
					switch (dialogActionId) {
						case USE_OBJECT: {
							spawn(215384, player.getWorldMapInstance(), (float) 493.9681, (float) 519.1524, (float) 968.9094, (byte) 0);
							return useQuestObject(env, 0, 0, false, false);
						}
					}
				}

				case 798316: {
					switch (dialogActionId) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1011);
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							if (QuestService.collectItemCheck(env, true)) {
								if (!giveQuestItem(env, 182206057, 1)) {
									return true;
								}
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							} else {
								return sendQuestDialog(env, 10001);
							}
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798316) {
				removeQuestItem(env, 182206057, 1);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
