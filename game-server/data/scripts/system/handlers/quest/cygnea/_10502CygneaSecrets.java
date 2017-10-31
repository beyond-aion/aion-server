package quest.cygnea;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @Author Majka
 * @Description
 * 							Talk with Nixkon at the Henor Territory Village.
 *              Look for Nixkon in Hiding in the Black Fin Village.
 *              Collect the Shining Soil Sample from the Shining Lump of Soil and report back to Nixkon in Hiding.
 *              Talk with Nixkon in Hiding at the Black Fin Village.
 *              Use the Rubbing Tool on the Unusual Mural in the Black Fin Village.
 *              Talk with Nixkon in Hiding at the Black Fin Village.
 *              Examine the Ancient Creature Fossil.
 *              Report back to Nixkon at the Henor Territory Village.
 *              Help Nixkon unravel the secrets of Cygnea.
 */
public class _10502CygneaSecrets extends AbstractQuestHandler {

	private final static int workItemId = 182215601; // Rubbing Tool Bag

	public _10502CygneaSecrets() {
		super(10502);
	}

	@Override
	public void register() {

		// Nixkon 804701
		// Nixkon in Hiding 804702
		// Shining Lump of Soil 702669
		// Ancient Fossil 731537
		int[] npcs = { 702669, 731537, 804701, 804702 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestItem(workItemId, questId);
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		switch (targetId) {
			case 804701: // Nixkon
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 0) { // Step 0: Talk with Nixkon at the Henor Territory Village.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1011);

						if (dialogActionId == SETPRO1)
							return defaultCloseDialog(env, var, var + 1);
					}
				}
				if (qs.getStatus() == QuestStatus.REWARD) {
					if (dialogActionId == USE_OBJECT) {
						return sendQuestDialog(env, 10002);
					}

					return sendQuestEndDialog(env);
				}
				break;
			case 804702: // Nixkon in Hiding
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 1) { // Step 1: Look for Nixkon in Hiding in the Black Fin Village.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1352);

						if (dialogActionId == SETPRO2)
							return defaultCloseDialog(env, var, var + 1);
					}

					if (var == 2) { // Step 2: Collect the Shining Soil Sample from the Shining Lump of Soil and report back to Nixkon in Hiding.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1693);

						if (dialogActionId == CHECK_USER_HAS_QUEST_ITEM) {
							long itemCount = player.getInventory().getItemCountByItemId(182215600);
							if (itemCount >= 3) {
								removeQuestItem(env, 182215600, itemCount);
								qs.setQuestVar(var + 1);
								updateQuestStatus(env);
								return sendQuestDialog(env, 10000);
							} else {
								return sendQuestDialog(env, 10001);
							}
						}
					}

					if (var == 3) { // Step 3: Talk with Nixkon in Hiding at the Black Fin Village.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 2034);
						if (dialogActionId == SETPRO4)
							return defaultCloseDialog(env, var, var + 1, workItemId, 1);
					}

					if (var == 5) { // Step 5: Talk with Nixkon in Hiding at the Black Fin Village.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 2716);
						if (dialogActionId == SETPRO6) {
							long itemCount = player.getInventory().getItemCountByItemId(182215602);
							if (itemCount > 0) {
								removeQuestItem(env, 182215602, itemCount);
							}
							return defaultCloseDialog(env, var, var + 1);
						}
					}
				}
				if (qs.getStatus() == QuestStatus.REWARD) {
					if (dialogActionId == USE_OBJECT) {
						return sendQuestDialog(env, 10002);
					}

					return sendQuestEndDialog(env);
				}
				break;
			case 702669:
				if (dialogActionId == USE_OBJECT && var == 2) {
					return useQuestObject(env, 2, 2, false, 0);
				}
				break;
			case 731537:
				if (var == 6) { // Step 6: Examine the Ancient Creature Fossil.
					if (dialogActionId == QUEST_SELECT)
						return sendQuestDialog(env, 3057);
					if (dialogActionId == SET_SUCCEED) {
						giveQuestItem(env, 182215635, 1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return defaultCloseDialog(env, var, var + 1);
					}
				}
				break;
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();

		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return HandlerResult.FAILED;

		final int id = item.getItemTemplate().getTemplateId();
		if (id != workItemId)
			return HandlerResult.FAILED;

		int var = qs.getQuestVarById(0);
		if (var == 4) { // Step 4: Use the Rubbing Tool Bag on the Unusual Mural in the Black Fin Village.
			giveQuestItem(env, 182215602, 1);
			return HandlerResult.fromBoolean(useQuestItem(env, item, var, var + 1, false));
		}

		return HandlerResult.SUCCESS;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 10500);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 10500);
	}
}
