package quest.inggison;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Cheatkiller
 */
public class _11147CuteBeadyEyes extends AbstractQuestHandler {

	public _11147CuteBeadyEyes() {
		super(11147);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798997).addOnQuestStart(questId);
		qe.registerQuestNpc(798997).addOnTalkEvent(questId);
		qe.registerQuestNpc(799079).addOnTalkEvent(questId);
		qe.registerQuestNpc(799081).addOnTalkEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("KLAWNICKTS_CAVE_210050000"), questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798997) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798997) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1011);
					else if (qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 1352);
				} else if (dialogActionId == SETPRO1) {
					spawnForFiveMinutesInFrontOf(799079, player, 1);
					spawnForFiveMinutesInFrontOf(799081, player, -1);
					return defaultCloseDialog(env, 0, 1);
				} else if (dialogActionId == SETPRO2) {
					return defaultCloseDialog(env, 1, 2);
				}
			} else if (targetId == 799079) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1693);
				} else if (dialogActionId == SETPRO3) {
					Npc npc = (Npc) env.getVisibleObject();
					npc.getController().delete();
					return defaultCloseDialog(env, 2, 3);
				}
			} else if (targetId == 799081) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 2034);
				} else if (dialogActionId == SETPRO4) {
					Npc npc = (Npc) env.getVisibleObject();
					npc.getController().delete();
					return defaultCloseDialog(env, 3, 4);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798997) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 10002);
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName == ZoneName.get("KLAWNICKTS_CAVE_210050000")) {
			Player player = env.getPlayer();
			if (player == null)
				return false;
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);
				if (var == 4) {
					changeQuestStep(env, 4, 4, true);
					return true;
				}
			}
		}
		return false;
	}
}
