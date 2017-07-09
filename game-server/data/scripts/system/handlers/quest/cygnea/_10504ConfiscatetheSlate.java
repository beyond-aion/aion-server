package quest.cygnea;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @Author Majka
 * @Description
 * 							Talk with Atrape at Andrea Territory Village.
 *              Eliminate the Beritra Troopers and Beritra Sentinels (5).
 *              Use the Flame Pepper Bomb to torture and obtain information from Commander Nazracht (1).
 *              Obtain the Broken Slate Piece from the Cracked Oath Slate and take it to Atrape.
 *              Help Atrape pursue the Beritra army and get information on Tiamat's secret treasure.
 */
public class _10504ConfiscatetheSlate extends AbstractQuestHandler {

	private final static int workItem = 182215606; // Flame Pepper Bomb
	private final static int collectItemId = 182215607; // Broken Slate Piece

	public _10504ConfiscatetheSlate() {
		super(10504);
	}

	@Override
	public void register() {
		// Cracked Oath Slate 702671
		// Atrape 804706
		int[] npcs = { 702671, 804706 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		int[] mobs = { 236253, 236254, 236255 }; // Beritra Troopers, Beritra Sentinels, Commander Nazracht
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		qe.registerQuestItem(collectItemId, questId);
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
			case 804706: // Atrape
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 0) { // Step 0: Talk with Atrape at Andrea Territory Village.
						if (dialogActionId == QUEST_SELECT) {
							return sendQuestDialog(env, 1011);
						}

						if (dialogActionId == SETPRO1) {
							return defaultCloseDialog(env, 0, 1, workItem, 1); // 1
						}
					}

					if (var == 3) { // Step 2: Obtain the Old Balaur Document from the Scattered Balaur Document and take it to Euvia
						if (dialogActionId == QUEST_SELECT) {
							return sendQuestDialog(env, 2034);
						}

						if (dialogActionId == CHECK_USER_HAS_QUEST_ITEM) {
							long itemCount = player.getInventory().getItemCountByItemId(collectItemId);
							if (itemCount > 0) {
								removeQuestItem(env, collectItemId, itemCount);
								qs.setQuestVar(var + 1);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 10000);
							} else {
								return sendQuestDialog(env, 10001);
							}
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
			case 702671:
				if (dialogActionId == USE_OBJECT && var == 3) { // Step 3: Obtain the Broken Slate Piece from the Cracked Oath Slate and take it to
																															// Atrape.
					return useQuestObject(env, 3, 3, false, 0);
				}
				break;
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		// Step 1: Eliminate the Beritra Troopers and Beritra Sentinels (5).
		// Step 2: Use the Flame Pepper Bomb to torture and obtain information from Commander Nazracht (1).
		switch (targetId) {
			case 236253:
			case 236254:
				if (var == 1) {
					int varKill = qs.getQuestVarById(1); // Kill counter
					qs.setQuestVarById(1, varKill + 1); // 1-5 with varNum 1
					if (varKill >= 4) {
						qs.setQuestVar(var + 1);
					}
					updateQuestStatus(env);
					return true;
				}
				break;
			case 236255:
				if (var == 2) {
					qs.setQuestVar(var + 1);
					updateQuestStatus(env);
					return true;
				}
				break;
		}
		return false;
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
