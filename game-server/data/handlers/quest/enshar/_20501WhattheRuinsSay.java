package quest.enshar;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @Author Majka
 * @Description:
 * 							Meet with Bonat in Sky Fortress Insurgent Post.
 *               Meet and talk with Nixie.
 *               Go to Dreg Findspot.
 *               Investigate the Crumbled Ruins.
 *               Investigate the Destroyed Balaur Ruins nearby.
 *               Look for the Intact Balaur Ruins.
 *               Examine the Putrid Balaur Corpse.
 *               Report back to Nixie.
 *               Order: Centurion Bonat is looking for you. Go and see him.
 */
public class _20501WhattheRuinsSay extends AbstractQuestHandler {

	private final static int workItemId = 182215637; // Enshar Ruins Piece

	public _20501WhattheRuinsSay() {
		super(20501);
	}

	@Override
	public void register() {
		// Bonat 804720
		// Nixie 804721
		// Crumbled Ruins 731538
		// Destroyed Balaur Ruins 731539
		// Intact Balaur Ruins 731540
		// Putrid Balaur Corpse 804722
		int[] npcs = { 731538, 731539, 731540, 804720, 804721, 804722 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerOnEnterZone(ZoneName.get("DF5_SENSORYAREA_Q20501A_206375_1_220080000"), questId); // Dreg Findspot zone
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
			case 804720: // Bonat
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 0) { // Step 0: Meet with Bonat in Sky Fortress Insurgent Post.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1011);

						if (dialogActionId == SETPRO1)
							return defaultCloseDialog(env, var, var + 1);
					}
				}
				break;
			case 804721: // Nixie
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 1) { // Step 1: Meet and talk with Nixie.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1352);

						if (dialogActionId == SETPRO2)
							return defaultCloseDialog(env, var, var + 1, workItemId, 1); // Gives item Ruins Location Map [ID: 182215598]
					}
				}

				if (qs.getStatus() == QuestStatus.REWARD) { // Step 7: Report back to Nixie.
					if (dialogActionId == USE_OBJECT) {
						return sendQuestDialog(env, 10002);
					}

					return sendQuestEndDialog(env);
				}
				break;
			case 731538: // Crumbled Ruins
				if (var == 3) { // Step 3: Investigate the Crumbled Ruins.
					if (dialogActionId == QUEST_SELECT)
						return sendQuestDialog(env, 2034);
					if (dialogActionId == SETPRO4) {
						return defaultCloseDialog(env, var, var + 1);
					}
				}
				break;
			case 731539: // Destroyed Balaur Ruins
				if (var == 4) { // Step 4: Investigate the Destroyed Balaur Ruins
					if (dialogActionId == QUEST_SELECT)
						return sendQuestDialog(env, 2375);
					if (dialogActionId == SETPRO5)
						return defaultCloseDialog(env, var, var + 1);
				}
				break;
			case 731540: // Intact Balaur Ruins
				if (var == 5) { // Step 5: Look for the Intact Balaur Ruins.
					if (dialogActionId == QUEST_SELECT)
						return sendQuestDialog(env, 2716);
					if (dialogActionId == SETPRO6)
						return defaultCloseDialog(env, var, var + 1);
				}
				break;
			case 804722: // Putrid Balaur Corpse.
				if (var == 6) { // Step 6: Examine the Putrid Balaur Corpse.
					if (dialogActionId == QUEST_SELECT)
						return sendQuestDialog(env, 3057);
					if (dialogActionId == SET_SUCCEED) {
						// Beritra Special Assassin [ID: 219935] is spawned
						spawnForFiveMinutes(219935, env.getVisibleObject().getPosition());
						qs.setQuestVar(var + 1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}
				}
				break;
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) { // Step 2: Go to Dreg Findspot.

		if (zoneName == ZoneName.get("DF5_SENSORYAREA_Q20501A_206375_1_220080000")) {

			Player player = env.getPlayer();
			if (player == null) {
				return false;
			}

			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);

				if (var == 2) {
					qs.setQuestVar(var + 1);
					updateQuestStatus(env);
				}
			}
			return true;
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
