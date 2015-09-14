package quest.sarpan;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Cheatkiller
 */
public class _41405BombingAreaRun extends QuestHandler {

	private final static int questId = 41405;

	private final static int mob = 282918;

	public _41405BombingAreaRun() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(mob).addOnKillEvent(questId);
		qe.registerQuestNpc(mob).addOnAddAggroListEvent(getQuestId());
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == mob) {
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				return QuestService.finishQuest(env);
			}
		}
		return false;
	}

	@Override
	public boolean onAddAggroListEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			QuestService.startQuest(env);
			return true;
		}
		return false;
	}
}
