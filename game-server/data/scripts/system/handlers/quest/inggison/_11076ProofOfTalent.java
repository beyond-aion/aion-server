package quest.inggison;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * @author Cheatkiller
 */
public class _11076ProofOfTalent extends AbstractQuestHandler {

	public _11076ProofOfTalent() {
		super(11076);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799025).addOnQuestStart(questId);
		qe.registerQuestNpc(799084).addOnTalkEvent(questId);
		qe.registerQuestNpc(799025).addOnTalkEvent(questId);
		qe.registerOnEnterWindStream(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799025) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799084) {
				if (dialogActionId == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 3)
						return sendQuestDialog(env, 2034);
				} else if (dialogActionId == SETPRO4) {
					TeleportService.teleportTo(player, 210050000, 1338.6f, 279.6f, 590, (byte) 80, TeleportAnimation.FADE_OUT_BEAM);
					return defaultCloseDialog(env, 3, 4, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799025) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWindStreamEvent(QuestEnv env, int teleportId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.getWorldId() == 210050000) {
				if (teleportId == 152001)
					changeQuestStep(env, 0, 1);
				else if (teleportId == 153001)
					changeQuestStep(env, 1, 2);
				else if (teleportId == 154001)
					changeQuestStep(env, 2, 3);
				return true;
			}
		}
		return false;
	}
}
