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
public class _2843OperationAnnihilate extends AbstractQuestHandler {

	public _2843OperationAnnihilate() {
		super(2843);
	}

	@Override
	public void register() {
		int[] mobs = { 215094, 215095, 215096, 215097, 215098, 215099, 215100, 215101, 215102, 215103, 215104, 215105, 215106, 215107, 215108, 215109, 215110, 215111, 215112, 215113, 215114, 215115, 215116, 215117, 215118, 215119, 215120, 215121, 215122, 215123, 215124, 215125, 215126, 215127, 215128, 215129, 215130, 215131, 215132, 215133, 215135, 215136, 215285, 215286, 215287, 215288, 215289, 215290, 215291, 215292, 215293, 215294, 215295, 215296, 215297, 215298, 215299, 215300, 215301, 215302, 215303, 215304, 215305, 215306, 215307, 215308, 215309, 215310, 215311, 215312, 215313, 215314, 215315, 215316 };
		qe.registerQuestNpc(268081).addOnQuestStart(questId);
		qe.registerQuestNpc(268081).addOnTalkEvent(questId);
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
			if (env.getTargetId() == 268081) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (env.getTargetId() == 268081)
				return true;
		} else if (qs.getStatus() == QuestStatus.REWARD && env.getTargetId() == 268081) {
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
			if (player.getPosition().getMapId() == 300140000) {
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
