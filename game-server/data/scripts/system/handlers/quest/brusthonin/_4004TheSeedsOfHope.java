package quest.brusthonin;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Nephis
 */
public class _4004TheSeedsOfHope extends AbstractQuestHandler {

	public _4004TheSeedsOfHope() {
		super(4004);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205128).addOnQuestStart(questId); // Randet
		qe.registerQuestNpc(205128).addOnTalkEvent(questId); // Randet
		qe.registerQuestNpc(700340).addOnTalkEvent(questId); // Earth Mound
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (targetId == 205128) { // Randet
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205128) {
				if (env.getDialogActionId() == USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else
					return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			final int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 700340: // Earth Mound
					if (env.getDialogActionId() == USE_OBJECT) {
						if (var < 4) {
							return useQuestObject(env, var, var + 1, false, true);
						} else if (var == 4) {
							return useQuestObject(env, 4, 4, true, true); // reward
						}
					}
			}
		}
		return false;
	}
}
