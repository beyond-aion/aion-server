package quest.pangaea;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Pad
 */
public class _14220NewZoneNewRules extends AbstractQuestHandler {

	private static final int startEndNpcId = 802540;
	private static final int talkNpcId = 802541;
	private static final List<Integer> belusNpcIds = new ArrayList<>(Arrays.asList(802544, 804080, 804081, 804082, 804689));
	private static final List<Integer> aspidaNpcIds = new ArrayList<>(Arrays.asList(802545, 804083, 804084, 804085, 804690));
	private static final List<Integer> atanatosNpcIds = new ArrayList<>(Arrays.asList(802546, 804086, 804087, 804088, 804691));
	private static final List<Integer> disillonNpcIds = new ArrayList<>(Arrays.asList(802547, 804089, 804090, 804091, 804692));

	public _14220NewZoneNewRules() {
		super(14220);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(startEndNpcId).addOnQuestStart(questId);
		qe.registerQuestNpc(startEndNpcId).addOnTalkEvent(questId);
		qe.registerQuestNpc(talkNpcId).addOnTalkEvent(questId);
		for (int belusNpcId : belusNpcIds)
			qe.registerQuestNpc(belusNpcId).addOnTalkEvent(questId);
		for (int aspidaNpcId : aspidaNpcIds)
			qe.registerQuestNpc(aspidaNpcId).addOnTalkEvent(questId);
		for (int atanatosNpcId : atanatosNpcIds)
			qe.registerQuestNpc(atanatosNpcId).addOnTalkEvent(questId);
		for (int disillonNpcId : disillonNpcIds)
			qe.registerQuestNpc(disillonNpcId).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == startEndNpcId) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var0 = qs.getQuestVarById(0);
			if (var0 == 0) {
				if (targetId == talkNpcId) {
					if (dialogActionId == QUEST_SELECT) {
						return sendQuestDialog(env, 1011);
					} else if (dialogActionId == SETPRO1) {
						return defaultCloseDialog(env, 0, 1);
					}
				}
			} else if (var0 == 1) {
				if (dialogActionId == QUEST_SELECT) {
					if (belusNpcIds.contains(targetId)) {
						return sendQuestDialog(env, 1352);
					} else if (aspidaNpcIds.contains(targetId)) {
						return sendQuestDialog(env, 1693);
					} else if (atanatosNpcIds.contains(targetId)) {
						return sendQuestDialog(env, 2034);
					} else if (disillonNpcIds.contains(targetId)) {
						return sendQuestDialog(env, 2375);
					}
				} else if (dialogActionId == SETPRO2) {
					return defaultCloseDialog(env, 1, 1, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == startEndNpcId) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

}
