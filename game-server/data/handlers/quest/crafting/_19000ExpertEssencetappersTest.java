package quest.crafting;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi, vlog
 */
public class _19000ExpertEssencetappersTest extends AbstractQuestHandler {

	private static final int itemId1 = 152003004;
	private static final int itemId2 = 152003005;
	private static final int itemId3 = 152003006;

	public _19000ExpertEssencetappersTest() {
		super(19000);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203780).addOnQuestStart(questId);
		qe.registerQuestNpc(203780).addOnTalkEvent(questId);
		qe.registerQuestNpc(203781).addOnTalkEvent(questId);
		qe.registerOnGetItem(122001250, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203780) { // Cornelius
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 203781: // Sabotes
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						case SETPRO1:
							giveQuestItem(env, 122001250, 1);
							return sendQuestSelectionDialog(env);
					}
					break;
				case 203780: // Cornelius
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						case CHECK_USER_HAS_QUEST_ITEM:
							long itemCount1 = player.getInventory().getItemCountByItemId(itemId1);
							long itemCount2 = player.getInventory().getItemCountByItemId(itemId2);
							long itemCount3 = player.getInventory().getItemCountByItemId(itemId3);
							if (itemCount1 >= 1 && itemCount2 >= 1 && itemCount3 >= 1) {
								removeQuestItem(env, itemId1, itemCount1);
								removeQuestItem(env, itemId2, itemCount2);
								removeQuestItem(env, itemId3, itemCount3);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 5);
							} else {
								return sendQuestDialog(env, 10001);
							}
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203780) { // Cornelius
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onGetItemEvent(QuestEnv env) {
		return defaultOnGetItemEvent(env, 0, 1, false); // 1
	}
}
