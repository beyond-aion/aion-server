package quest.pandaemonium;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _2948HuronsLetter extends AbstractQuestHandler {

	public _2948HuronsLetter() {
		super(2948);
	}

	@Override
	public void register() {
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(204274).addOnTalkEvent(questId);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		if (targetId != 204274)
			return false;
		if (qs.getStatus() == QuestStatus.START) {
			if (env.getDialogActionId() == QUEST_SELECT)
				return sendQuestDialog(env, 10002);
			else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
				qs.setStatus(QuestStatus.REWARD);
				qs.setQuestVarById(0, 1);
				updateQuestStatus(env);
				return sendQuestDialog(env, 5);
			}
			return false;
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env);
		}
		return false;
	}
}
