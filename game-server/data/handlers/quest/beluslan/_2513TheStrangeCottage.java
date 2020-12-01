package quest.beluslan;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _2513TheStrangeCottage extends AbstractQuestHandler {

	public _2513TheStrangeCottage() {
		super(2513);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204732).addOnQuestStart(questId); // Gnalin
		qe.registerQuestNpc(204732).addOnTalkEvent(questId);
		qe.registerQuestNpc(204827).addOnTalkEvent(questId); // Hild
		qe.registerQuestNpc(204826).addOnTalkEvent(questId); // Freki
		qe.registerQuestNpc(790022).addOnTalkEvent(questId); // Byggvir
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204732) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 204826) {// Freki
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1011);
						return false;
					case SETPRO1:
						if (var == 0) {
							qs.setRewardGroup(0);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						}
				}
			}
			if (targetId == 204827) {// Hild
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0) {
							return sendQuestDialog(env, 1352);
						}
						return false;
					case SETPRO2:
						if (var == 0) {
							qs.setRewardGroup(1);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						}
				}
			}
			if (targetId == 790022) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0) {
							return sendQuestDialog(env, 1693);
						}
						return false;
					case SETPRO3:
						if (var == 0) {
							qs.setRewardGroup(2);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return closeDialogWindow(env);
						}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204732) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 10002);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
