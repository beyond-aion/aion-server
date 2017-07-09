package quest.gelkmaros;

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
public class _21080MessageInAWindstream extends AbstractQuestHandler {

	public _21080MessageInAWindstream() {
		super(21080);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799231).addOnQuestStart(questId);
		qe.registerQuestNpc(799231).addOnTalkEvent(questId);
		qe.registerQuestNpc(799427).addOnTalkEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("ANTAGOR_CANYON_220070000"), questId);
		qe.registerOnEnterZone(ZoneName.get("GELKMAROS_FORTRESS_220070000"), questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799231) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env, 182207939, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799427) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 3)
						return sendQuestDialog(env, 2034);
				} else if (dialogActionId == SETPRO4) {
					removeQuestItem(env, 182207939, 1);
					return defaultCloseDialog(env, 3, 4);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799231) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (zoneName == ZoneName.get("ANTAGOR_CANYON_220070000")) {
				if (var < 3) {
					changeQuestStep(env, var, var + 1);
					return true;
				}
			} else if (zoneName == ZoneName.get("GELKMAROS_FORTRESS_220070000") && var == 4) {
				changeQuestStep(env, 4, 4, true);
				return true;
			}
		}
		return false;
	}
}
