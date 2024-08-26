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
public class _1844TargetBalaur extends AbstractQuestHandler {

	public _1844TargetBalaur() {
		super(1844);
	}

	@Override
	public void register() {
		int[] mobs = { 215180, 215181, 215182, 215183, 215184, 215185, 215186, 215187, 215188, 215189, 215190, 215191, 215192, 215193, 215194, 215195,
			215196, 215197, 215198, 215199, 215200, 215201, 215202, 215203, 215204, 215205, 215206, 215207, 215208, 215209, 215210, 215211, 215212, 215213,
			215214, 215215, 215216, 215217, 215218, 215219, 215221, 215222, 215349, 215350, 215351, 215352, 215353, 215354, 215355, 215356, 215357, 215358,
			215359, 215360, 215361, 215362, 215363, 215364, 215365, 215366, 215367, 215368, 215369, 215370, 215371, 215372, 215373, 215374, 215375, 215376,
			215377, 215378, 215379, 215380, 215220 };
		qe.registerQuestNpc(270165).addOnQuestStart(questId);
		qe.registerQuestNpc(270165).addOnTalkEvent(questId);
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
			if (env.getTargetId() == 270165) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == 270165)
				return false;
		} else if (qs.getStatus() == QuestStatus.REWARD && env.getTargetId() == 270165) {
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
			if (player.getPosition().getMapId() == 300130000) {
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
