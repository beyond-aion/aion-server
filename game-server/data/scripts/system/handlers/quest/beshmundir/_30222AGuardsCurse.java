package quest.beshmundir;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog
 */
public class _30222AGuardsCurse extends QuestHandler {

	private static final int questId = 30222;

	public _30222AGuardsCurse() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798979).addOnQuestStart(questId);
		qe.registerQuestNpc(798979).addOnTalkEvent(questId);
		qe.registerQuestNpc(216739).addOnKillEvent(questId);
		qe.registerQuestNpc(216239).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 798979) { // Gelon
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 798979) { // Gelon
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 1) {
							return sendQuestDialog(env, 1352);
						}
					}
					case SELECT_QUEST_REWARD: {
						changeQuestStep(env, 1, 1, true); // reward
						return sendQuestDialog(env, 5);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798979) { // Gelon
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
			// case 216739: { // Warrior Monument
			// Npc npc = (Npc) env.getVisibleObject();
			// QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 216239, npc.getX(), npc.getY(),
			// npc.getZ(), npc.getHeading());
			// return true;
			// }
				case 216239: { // Ahbana the Wicked
					return defaultOnKillEvent(env, 216239, 0, 1); // 1
				}
			}
		}
		return false;
	}
}
