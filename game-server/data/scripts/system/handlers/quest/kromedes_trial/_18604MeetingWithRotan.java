package quest.kromedes_trial;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestActionType;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Rolandas
 * @modified Pad
 */
public class _18604MeetingWithRotan extends QuestHandler {

	private static final int questId = 18604;

	public _18604MeetingWithRotan() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZone(ZoneName.get("GRAND_CAVERN_300230000"), questId);
		qe.registerQuestNpc(700961).addOnTalkEvent(questId); // Grave Robber's Corpse
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			return QuestService.startQuest(env);
		}

		if (env.getTargetId() == 700961) {
			if (env.getDialog() == DialogAction.USE_OBJECT) {
				if (checkItemExistence(env, 164000141, 1, false)) {
					env.setQuestId(0);
					return sendQuestDialog(env, 27);
				}
				if (qs.getStatus() == QuestStatus.START) {
					return sendQuestDialog(env, 10002);
				} else {
					giveQuestItem(env, 164000141, 1);
					env.setQuestId(0);
					return sendQuestDialog(env, 1012);
				}
			} else if (env.getDialog() == DialogAction.SELECT_QUEST_REWARD) {
				return sendQuestDialog(env, 5);
			} else {
				changeQuestStep(env, 0, 0, true);
				return sendQuestEndDialog(env);
			}
		}

		return false;
	}

	@Override
	public boolean onCanAct(QuestEnv env, QuestActionType questEventType, Object... objects) {
		// allow to use body even when quest is completed
		return env.getTargetId() == 700961 && questEventType == QuestActionType.ACTION_ITEM_USE;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName != ZoneName.get("GRAND_CAVERN_300230000"))
			return false;

		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			env.setQuestId(questId);
			return QuestService.startQuest(env);
		}

		return false;
	}

}
