package quest.enshar;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * Talk with Sefrim.
 * Talk with Julia.
 * Investigate the Mindboggle Waste.
 * Eliminate the Enigmatic Drakan who appears.
 * Defeat the Interhypno Ego.
 * Talk with Jadun.
 * Talk with Julia.
 * Talk with Sefrim.
 * Order: Sefrim is looking for you. Go and see him.
 *
 * @author Majka, Pad
 */
public class _20506MuscleOverMind extends AbstractQuestHandler {

	public _20506MuscleOverMind() {
		super(20506);
	}

	@Override
	public void register() {
		// Sefrim 804736
		// Julia 804737
		// Jadun 804743
		int[] npcs = { 804736, 804737, 804743 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		int[] mobs = { 219956, 219957 };
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		qe.registerOnEnterZone(ZoneName.get("DF5_SENSORYAREA_Q20506A_206376_2_220080000"), questId); // Mindboggle Waste zone
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerOnLogOut(questId);
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
			case 804736: // Sefrim
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 0) { // Step 0: Talk with Sefrim.
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
			case 804737: // Julia
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 1) { // Step 1: Talk with Julia.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1352);

						if (dialogActionId == SETPRO2)
							return defaultCloseDialog(env, var, var + 1);
					}

					if (var == 6) { // Step 6: Talk with Julia.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 3057);

						if (dialogActionId == SET_SUCCEED) {
							qs.setStatus(QuestStatus.REWARD);
							qs.setQuestVar(var + 1);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						}
					}
				}
				break;
			case 804743: // Jadun
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 5) { // Step 5: Talk with Jadun.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 2716);

						if (dialogActionId == SETPRO6)
							return defaultCloseDialog(env, var, var + 1);
					}
				}
				break;
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName == ZoneName.get("DF5_SENSORYAREA_Q20506A_206376_2_220080000")) {
			Player player = env.getPlayer();
			if (player == null)
				return false;

			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);

				if (var == 2) { // Step 2: Investigate the Mindboggle Waste.
					qs.setQuestVar(var + 1);
					updateQuestStatus(env);
					spawnTemporarily(219956, player.getWorldMapInstance(), 1938.0f, 83.9f, 235.0f, (byte) 90, 10);
					ThreadPoolManager.getInstance().schedule(() -> restoreQuestStep(env), 600000);
				}
			}
			return true;
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

		switch (targetId) {
			case 219956:
				if (var == 3) { // Step 3: Eliminate the Enigmatic Drakan who appears.
					qs.setQuestVar(var + 1);
					updateQuestStatus(env);
					spawnTemporarily(219957, player.getWorldMapInstance(), 1938.0f, 83.9f, 235.0f, (byte) 90, 8);
					return true;
				}
				break;
			case 219957:
				if (var == 4) { // Step 4: Defeat the Interhypno Ego.
					qs.setQuestVar(var + 1);
					updateQuestStatus(env);
					spawnTemporarily(804743, player.getWorldMapInstance(), 1938.0f, 83.9f, 235.0f, (byte) 90, 6); // Jadun
					return true;
				}
				break;
		}
		return false;
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		return restoreQuestStep(env);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 20500);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 20500);
	}

	private boolean restoreQuestStep(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		if (var >= 3 && var <= 5) {
			qs.setQuestVar(2);
			updateQuestStatus(env);
		}
		return true;
	}
}
