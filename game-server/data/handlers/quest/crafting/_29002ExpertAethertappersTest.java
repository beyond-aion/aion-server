package quest.crafting;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Gigi, Pad
 */
public class _29002ExpertAethertappersTest extends AbstractQuestHandler {

	private static final int itemId1 = 152003007;
	private static final int itemId2 = 152003008;

	public _29002ExpertAethertappersTest() {
		super(29002);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204257).addOnQuestStart(questId);
		qe.registerQuestNpc(204257).addOnTalkEvent(questId);
		qe.registerQuestNpc(204099).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204257) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204099:
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1011);
						case SETPRO1:
							if (!giveQuestItem(env, 122001251, 1))
								return true;
							qs.setQuestVarById(0, 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
					}
					return false;
				case 204257:
					switch (dialogActionId) {
						case QUEST_SELECT: {
							long itemCount1 = player.getInventory().getItemCountByItemId(itemId1);
							long itemCount2 = player.getInventory().getItemCountByItemId(itemId2);
							if (itemCount1 >= 1 && itemCount2 >= 1) {
								removeQuestItem(env, itemId1, itemCount1);
								removeQuestItem(env, itemId2, itemCount2);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 5);
							} else {
								return sendQuestDialog(env, 10001);
							}
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204257) {
				if (dialogActionId == CHECK_USER_HAS_QUEST_ITEM)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
