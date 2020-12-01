package quest.enshar;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @Author Majka
 * @Description:
 * 							Talk with Walque.
 *               Talk with Kronag.
 *               Talk with Kanovi.
 *               Get a Highland Husk and take it to Kanovi.
 *               Talk with Kanovi.
 *               Talk with Muratun.
 *               Take the Tejhi Trust Token that Muratun gave you to Batuga.
 *               Report to Walque.
 *               Order: Meet with Walqule.
 */
public class _20502EvolvingMysteries extends AbstractQuestHandler {

	public _20502EvolvingMysteries() {
		super(20502);
	}

	@Override
	public void register() {
		// Walque 804723
		// Kronag 804724
		// Kanovi 804725
		// Muratun 804726
		// Batuga 804727
		int[] npcs = { 804723, 804724, 804725, 804726, 804727 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
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
			case 804723: // Walque
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 0) { // Step 0: Talk with Walque.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1011);

						if (dialogActionId == SETPRO1)
							return defaultCloseDialog(env, var, var + 1);
					}
				}

				if (qs.getStatus() == QuestStatus.REWARD) { // Step 8: Report to Walque.
					if (dialogActionId == USE_OBJECT) {
						return sendQuestDialog(env, 10002);
					}

					return sendQuestEndDialog(env);
				}
				break;
			case 804724: // Kronag
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 1) { // Step 1: Talk with Kronag.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1352);

						if (dialogActionId == SETPRO2)
							return defaultCloseDialog(env, var, var + 1);
					}
				}
				break;
			case 804725: // Kanovi
				if (var == 2) { // Step 2: Talk with Kanovi.
					if (dialogActionId == QUEST_SELECT)
						return sendQuestDialog(env, 1693);
					if (dialogActionId == SETPRO3) {
						return defaultCloseDialog(env, var, var + 1);
					}
				}

				if (var == 3) { // Step 3: Get a Highland Husk and take it to Kanovi
					if (dialogActionId == QUEST_SELECT)
						return sendQuestDialog(env, 2034);
					if (dialogActionId == CHECK_USER_HAS_QUEST_ITEM) {
						long itemCount = player.getInventory().getItemCountByItemId(182215639); // Highland Husk
						if (itemCount >= 5) {
							removeQuestItem(env, 182215639, itemCount);
							qs.setQuestVar(var + 1);
							updateQuestStatus(env);
							return sendQuestDialog(env, 10000);
						} else {
							return sendQuestDialog(env, 10001);
						}
					}
				}

				if (var == 4) { // Step 4: Talk with Kanovi.
					if (dialogActionId == QUEST_SELECT)
						return sendQuestDialog(env, 2375);
					if (dialogActionId == SETPRO5) {
						return defaultCloseDialog(env, var, var + 1);
					}
				}
				break;
			case 804726: // Muratun
				if (var == 5) { // Step 5: Talk with Muratun.
					if (dialogActionId == QUEST_SELECT)
						return sendQuestDialog(env, 2716);
					if (dialogActionId == SETPRO6)
						return defaultCloseDialog(env, var, var + 1, 182215638, 1); // Token of Friendship
				}
				break;
			case 804727: // Batuga
				if (var == 6) { // Step 6: Take the Tejhi Trust Token that Muratun gave you to Batuga.
					if (dialogActionId == QUEST_SELECT)
						return sendQuestDialog(env, 3057);
					if (dialogActionId == SET_SUCCEED) {
						removeQuestItem(env, 182215638, 1);
						qs.setStatus(QuestStatus.REWARD);
						qs.setQuestVar(var + 1);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}
				}
				break;
		}
		return false;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 20500);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 20500);
	}
}
