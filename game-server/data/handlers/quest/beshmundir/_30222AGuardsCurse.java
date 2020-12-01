package quest.beshmundir;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog
 */
public class _30222AGuardsCurse extends AbstractQuestHandler {

	public _30222AGuardsCurse() {
		super(30222);
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
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798979) { // Gelon
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 798979) { // Gelon
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 1) {
							return sendQuestDialog(env, 1352);
						}
						return false;
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 1, 1, true); // reward
						return sendQuestDialog(env, 5);
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
				// spawn(216239, player, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
				// return true;
				// }
				case 216239: // Ahbana the Wicked
					return defaultOnKillEvent(env, 216239, 0, 1); // 1
			}
		}
		return false;
	}
}
