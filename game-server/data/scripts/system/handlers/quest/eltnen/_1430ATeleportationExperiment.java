package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * @author Xitanium
 */
public class _1430ATeleportationExperiment extends AbstractQuestHandler {

	public _1430ATeleportationExperiment() {
		super(1430);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203919).addOnQuestStart(questId); // Onesimus
		qe.registerQuestNpc(203919).addOnTalkEvent(questId); // Onesimus
		qe.registerQuestNpc(203337).addOnTalkEvent(questId); // Sonirim
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (targetId == 203919) { // Onesimus
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 203337) { // Sonirim

			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (env.getDialogActionId() == SETPRO1) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					qs.setStatus(QuestStatus.REWARD);
					TeleportService.teleportTo(player, 220020000, 1, 638, 2337, 425, (byte) 20, TeleportAnimation.FADE_OUT_BEAM);
					return true;
				} else
					return sendQuestStartDialog(env);
			} else if (qs.getStatus() == QuestStatus.REWARD) { // Reward
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4080);
				else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
					qs.setQuestVar(2);
					updateQuestStatus(env);
					return sendQuestEndDialog(env);
				} else
					return sendQuestEndDialog(env);
			}

		}
		return false;
	}
}
