package quest.chantra_dredgion;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog
 */
public class _4721RiseOfChantraDredgion extends AbstractQuestHandler {

	public _4721RiseOfChantraDredgion() {
		super(4721);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799226).addOnQuestStart(questId);
		qe.registerQuestNpc(799226).addOnTalkEvent(questId);
		qe.registerQuestNpc(799403).addOnTalkEvent(questId);
		qe.registerQuestNpc(700948).addOnTalkEvent(questId);
		qe.registerQuestNpc(216886).addOnKillEvent(questId);
		qe.addHandlerSideQuestDrop(questId, 700948, 182205691, 1, 100, 1);
		qe.registerOnGetItem(182205691, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799226) { // Valetta
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 799403) { // Yorgen
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0) {
							return sendQuestDialog(env, 1011);
						}
						return false;
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1); // 1
				}
			} else if (targetId == 700948) {
				return true;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799226) { // Valetta
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
	public boolean onGetItemEvent(QuestEnv env) {
		return defaultOnGetItemEvent(env, 1, 2, false); // 2
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 216886, 2, true); // reward
	}
}
