package quest.esoterrace;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Ritsu
 */
public class _28410FortressUnsecured extends AbstractQuestHandler {

	public _28410FortressUnsecured() {
		super(28410);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799587).addOnQuestStart(questId);
		qe.registerQuestNpc(799587).addOnTalkEvent(questId);
		qe.registerQuestNpc(799563).addOnTalkEvent(questId);
		qe.registerQuestNpc(799558).addOnTalkEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("DRANA_PRODUCTION_LAB_300250000"), questId);
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		Player player = env.getPlayer();
		if (player == null)
			return false;
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (zoneName != ZoneName.get("DRANA_PRODUCTION_LAB_300250000"))
			return false;
		if (qs == null || qs.getQuestVars().getQuestVars() != 1)
			return false;
		if (qs.getStatus() != QuestStatus.START)
			return false;
		qs.setStatus(QuestStatus.REWARD);
		updateQuestStatus(env);
		return true;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (targetId == 799587) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 799563) {
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (env.getDialogActionId() == SETPRO1)
					return defaultCloseDialog(env, 0, 1);
				else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 799558) {
			if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
				if (env.getDialogActionId() == USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
