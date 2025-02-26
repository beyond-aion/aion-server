package quest.heiron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestActionType;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Balthazar, fixed Shaman, vlog
 */
public class _1636AFluteForTheFixing extends AbstractQuestHandler {

	public _1636AFluteForTheFixing() {
		super(1636);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204535).addOnQuestStart(questId);
		qe.registerQuestNpc(204535).addOnTalkEvent(questId);
		qe.registerQuestNpc(700239).addOnTalkEvent(questId);
		qe.registerQuestNpc(203792).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204535) { // Maximus
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 203792: // Utsida
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							} else if (var == 1) {
								return sendQuestDialog(env, 1352);
							} else if (var == 2) {
								return sendQuestDialog(env, 2034);
							}
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
						case CHECK_USER_HAS_QUEST_ITEM:
							return checkQuestItems(env, 1, 2, false, 10000, 10001); // 2
						case SETPRO4:
							return defaultCloseDialog(env, 2, 3, 182201785, 1, 182201789, 1); // 3
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
					break;
				case 700239: // Drake Stone Statue
					return playQuestMovie(env, 210);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204535) { // Maximus
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
	public boolean onCanAct(QuestEnv env, QuestActionType questEventType, Object... objects) {
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(env.getQuestId());
		return env.getTargetId() == 700239 && qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 3;
	}

	@Override
	public void onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId == 210) {
			changeQuestStep(env, 3, 3, true);
			VisibleObject drakeStoneStatue = env.getVisibleObject();
			drakeStoneStatue.getController().deleteAndScheduleRespawn();
			spawnTemporarily(212008, drakeStoneStatue.getWorldMapInstance(), drakeStoneStatue.getX(), drakeStoneStatue.getY(), drakeStoneStatue.getZ(), (byte) 30, 60);
		}
	}
}
