package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog TODO: siege weapons attacking event, when siege weapons done
 */
public class _3702GeneralDestruction extends AbstractQuestHandler {

	public _3702GeneralDestruction() {
		super(3702);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(278517).addOnQuestStart(questId);
		qe.registerQuestNpc(278517).addOnTalkEvent(questId);
		qe.registerQuestNpc(256694).addOnKillEvent(questId);
		qe.registerQuestNpc(256693).addOnKillEvent(questId); // nochsana general
		qe.registerQuestNpc(290161).addOnKillEvent(questId); // nochsana general
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 278517) { // Nereus
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env, 182202179, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 278517) { // Nereus
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
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 0) {
				return defaultOnKillEvent(env, 256694, 0, 1); // 1
			} else if (var == 1) {
				return defaultOnKillEvent(env, new int[] { 256693, 290161 }, 1, true); // reward
			}
		}
		return false;
	}
}
