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
 * 							Talk with Cenute.
 *               Talk with Malite.
 *               Investigate the Field Wardens Bodyguard Corpse nearby.
 *               Investigate the Field Warden Corpse.
 *               Track down and kill the Beritra Research Corps Warmage in Timeswept Altar.
 *               Talk with Cenute.
 *               Order: See Cenute and carry out his mission.
 */
public class _20505AncientCrystal extends AbstractQuestHandler {

	public _20505AncientCrystal() {
		super(20505);
	}

	@Override
	public void register() {
		// Cenute 804732
		// Malite 804733
		int[] npcs = { 804732, 804733, 804734, 804735 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestNpc(219953).addOnKillEvent(questId);
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
			case 804732:
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 0) { // Step 0: Talk with Cenute.
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
			case 804733:
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 1) { // Step 1: Talk with Malite.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1352);

						if (dialogActionId == SETPRO2)
							return defaultCloseDialog(env, var, var + 1);
					}
				}
				break;
			case 804734:
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 2) { // Step 2: Investigate the Field Wardens Bodyguard Corpse nearby.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1693);

						if (dialogActionId == SETPRO3)
							return defaultCloseDialog(env, var, var + 1);
					}
				}
				break;
			case 804735:
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 3) { // Step 3: Investigate the Field Warden Corpse.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 2034);

						if (dialogActionId == SETPRO4)
							return defaultCloseDialog(env, var, var + 1);
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
			case 219953:
				if (var == 4) { // Step 4: Track down and kill the Beritra Research Corps Warmage in Timeswept Altar.
					qs.setStatus(QuestStatus.REWARD);
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
		defaultOnQuestCompletedEvent(env, 20500);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 20500);
	}
}
