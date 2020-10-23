package quest.enshar;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @Author Majka
 * @Description:
 * 							Talk with Xeikun.
 *               Investigate the Beritra troops (5) in The Beritra Defense Line.
 *               Talk with Xeikun.
 *               Investigate Vengeful Aetheric Guard Dominators (5) in the Wretched Plot.
 *               Take down Cursed Gilgamesh. (1)
 *               Talk with Gilgamesh.
 *               Talk with Jalturan somewhere near Cet Siege Territory.
 *               Report to Xeikun.
 *               Order: Xeikun is looking for you. Go and see him.
 */
public class _20504TiamatsShadow extends AbstractQuestHandler {

	public _20504TiamatsShadow() {
		super(20504);
	}

	@Override
	public void register() {
		// Xeikun 804730
		// Jalturan 804731
		// Gilgamesh 804742
		int[] npcs = { 804730, 804731, 804742 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}

		int[] mobs = { 219943, 219944, 219945, 219946, 219947, 219948, 219949 };
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerOnInvisibleTimerEnd(questId);
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
			case 804730: // Xeikun
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 0) { // Step 0: Talk with Xeikun.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1011);

						if (dialogActionId == SETPRO1)
							return defaultCloseDialog(env, var, var + 1);
					}

					if (var == 2) { // Step 2: Talk with Xeikun.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1693);

						if (dialogActionId == SETPRO3)
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
			case 804742: // Gilgamesh
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 5) { // Step 5: Talk with Gilgamesh.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 2716);

						if (dialogActionId == SETPRO6)
							return defaultCloseDialog(env, var, var + 1);
					}
				}
				break;
			case 804731: // Jalturan
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 6) { // Step 6: Talk with Jalturan somewhere near Cet Siege Territory.
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
			case 219943:
			case 219944:
			case 219945:
				if (var == 1) { // Step 1: Investigate the Beritra troops (5) in The Beritra Defense Line.
					int varKill = qs.getQuestVarById(1); // Kill counter
					qs.setQuestVarById(1, varKill + 1); // 1-5 with varNum 1
					if (varKill >= 4) {
						qs.setQuestVar(var + 1);
					}
					updateQuestStatus(env);
					return true;
				}
				break;
			case 219946:
			case 219947:
			case 219948:
				if (var == 3) { // Step 3: Investigate Vengeful Aetheric Guard Dominators (5) in the Wretched Plot.
					int varKill = qs.getQuestVarById(1); // Kill counter
					qs.setQuestVarById(1, varKill + 1); // 1-5 with varNum 1
					if (varKill >= 4) {
						qs.setQuestVar(var + 1);
					}
					updateQuestStatus(env);
					return true;
				}
				break;
			case 219949:
				if (var == 4) { // Step 4: Take down Cursed Gilgamesh. (1)
					qs.setQuestVar(var + 1);
					updateQuestStatus(env);
					spawnForFiveMinutes(804742, env.getVisibleObject().getPosition());
					QuestService.invisibleTimerStart(env, 300);
					return true;
				}
				break;
		}
		return false;
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		return RestoreQuestStep(env, 5, 4);
	}

	@Override
	public boolean onInvisibleTimerEndEvent(QuestEnv env) {
		return RestoreQuestStep(env, 5, 4);
	}

	private boolean RestoreQuestStep(QuestEnv env, int step, int oldStep) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		if (var == step) {
			qs.setQuestVar(oldStep);
			updateQuestStatus(env);
		}
		return true;
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
