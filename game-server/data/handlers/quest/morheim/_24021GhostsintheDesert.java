package quest.morheim;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Ritsu
 */
public class _24021GhostsintheDesert extends AbstractQuestHandler {

	private int itemId = 182215363;

	public _24021GhostsintheDesert() {
		super(24021);
	}

	@Override
	public void register() {
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerQuestItem(itemId, questId);
		qe.registerQuestNpc(204302).addOnTalkEvent(questId);
		qe.registerQuestNpc(204329).addOnTalkEvent(questId);
		qe.registerQuestNpc(802046).addOnTalkEvent(questId);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 24020);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 24020);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204302: // Bragi
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1011);
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
					}
					break;
				case 204329: // Tofa
					switch (dialogActionId) {
						case QUEST_SELECT:
							switch (var) {
								case 1: {
									return sendQuestDialog(env, 1352);
								}
								case 2: {
									return sendQuestDialog(env, 1693);
								}
								case 3: {
									return sendQuestDialog(env, 10000);
								}
							}
							return false;
						case SELECT2_1:
							if (var == 1) {
								playQuestMovie(env, 73);
								return sendQuestDialog(env, 1353);
							}
							return false;
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2); // 2
					}
					return false;
				case 802046: // Tofynir
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
							if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
							return false;
						case CHECK_USER_HAS_QUEST_ITEM:
							return checkQuestItems(env, 2, 3, false, 10000, 10001); // 3
						case SETPRO4:
							if (!player.getInventory().isFullSpecialCube()) {
								return defaultCloseDialog(env, 3, 4, 182215363, 1, 0, 0); // 4
							} else {
								return sendQuestSelectionDialog(env);
							}
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204329) { // Tofa
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (item.getItemId() == 182215363 && player.isInsideItemUseZone(ZoneName.get("DF2_ITEMUSEAREA_Q2032"))) {
				return HandlerResult.fromBoolean(useQuestItem(env, item, 4, 4, true, 88)); // reward
			}
		}
		return HandlerResult.FAILED;
	}
}
