package quest.udas_temple;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Cheatkiller
 */
public class _30111CenterOfTheWeb extends AbstractQuestHandler {

	public _30111CenterOfTheWeb() {
		super(30111);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799335).addOnQuestStart(questId);
		qe.registerQuestNpc(799335).addOnTalkEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("IDTEMPLE_SENSORYAREA_Q30011_206105_1_300160000"), questId);
		qe.registerQuestNpc(215792).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799335) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799335) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SELECT_QUEST_REWARD) {
					return defaultCloseDialog(env, 2, 2, true, true);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799335) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 5);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 215792, 1, true);
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 0) {
				changeQuestStep(env, 0, 1);
				return true;
			}
		}
		return false;
	}
}
