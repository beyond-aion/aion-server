package quest.gelkmaros;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Cheatkiller
 */
public class _21080MessageInAWindstream extends QuestHandler {

	private final static int questId = 21080;

	public _21080MessageInAWindstream() {
		super(questId);
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
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 799231) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env, 182207939, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799427) {
				if (dialog == DialogAction.QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 3)
						return sendQuestDialog(env, 2034);
				} else if (dialog == DialogAction.SETPRO4) {
					removeQuestItem(env, 182207939, 1);
					return defaultCloseDialog(env, 3, 4);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799231) {
				if (dialog == DialogAction.USE_OBJECT) {
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
