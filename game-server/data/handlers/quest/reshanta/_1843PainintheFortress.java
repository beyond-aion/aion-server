package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.QUEST_SELECT;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Hilgert
 */
public class _1843PainintheFortress extends AbstractQuestHandler {

	public _1843PainintheFortress() {
		super(1843);
	}

	@Override
	public void register() {
		int[] mobs = { 215137, 215138, 215139, 215140, 215141, 215142, 215143, 215144, 215145, 215146, 215147, 215148, 215149, 215150, 215151, 215152,
			215153, 215154, 215155, 215156, 215157, 215158, 215159, 215160, 215161, 215162, 215163, 215164, 215165, 215166, 215167, 215168, 215169, 215170,
			215171, 215172, 215173, 215174, 215175, 215176, 215178, 215179, 215317, 215318, 215319, 215320, 215321, 215322, 215323, 215324, 215325, 215326,
			215327, 215328, 215329, 215330, 215331, 215332, 215333, 215334, 215335, 215336, 215337, 215338, 215339, 215340, 215341, 215342, 215343, 215344,
			215345, 215346, 215347, 215348, 215177 };
		qe.registerQuestNpc(269265).addOnQuestStart(questId);
		qe.registerQuestNpc(269265).addOnTalkEvent(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		qe.registerOnEnterWorld(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (env.getTargetId() == 269265) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == 269265)
				return false;
		} else if (qs.getStatus() == QuestStatus.REWARD && env.getTargetId() == 269265) {
			qs.setQuestVarById(0, 0);
			updateQuestStatus(env);
			return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			if (player.getPosition().getMapId() == 300120000) {
				if (qs.getQuestVarById(0) < 79) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					return true;
				} else if (qs.getQuestVarById(0) == 79 || qs.getQuestVarById(0) > 79) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}
}
