package quest.gelkmaros;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _21105CoweringRefugee extends AbstractQuestHandler {

	public _21105CoweringRefugee() {
		super(21105);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799276).addOnQuestStart(questId);
		qe.registerQuestNpc(799276).addOnTalkEvent(questId);
		qe.registerQuestNpc(700812).addOnTalkEvent(questId);
		qe.registerQuestNpc(799366).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799276) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env, 182207857, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799366) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else if (dialogActionId == SET_SUCCEED) {
					Npc npc = (Npc) env.getVisibleObject();
					npc.getController().delete();
					removeQuestItem(env, 182207857, 1);
					return defaultCloseDialog(env, 0, 1, true, false);
				}
			} else if (targetId == 700812) {
				Npc npc = (Npc) env.getVisibleObject();
				spawnForFiveMinutes(Rnd.nextBoolean() ? 799366 : 216086, npc.getPosition());
				npc.getController().deleteAndScheduleRespawn();
				return true;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799276) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
