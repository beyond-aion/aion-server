/*
package quest.danuar_sanctuary;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;
*/

/**
 * @author Pad
 */

/*
public class _26987ExploretheElyosCorridor extends QuestHandler {

	private static final int questId = 26987;
	private static final int[] npcIds = { 804866, 804867 };

	public _26987ExploretheElyosCorridor() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(npcIds[0]).addOnQuestStart(questId);
		for (int npcId : npcIds)
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("LF5_SensoryArea_Q26987"), questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == npcIds[0]) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == npcIds[1]) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 10002);
				else if (dialog == DialogAction.SELECT_QUEST_REWARD)
					return defaultCloseDialog(env, 1, 1, true, true);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == npcIds[1])
				return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		changeQuestStep(env, 0, 1, false);
		return true;
	}
} */
