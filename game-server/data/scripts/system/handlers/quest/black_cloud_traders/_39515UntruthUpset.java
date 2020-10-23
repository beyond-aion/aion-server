package quest.black_cloud_traders;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Cheatkiller
 */
public class _39515UntruthUpset extends AbstractQuestHandler {

	private int[] mobs = { 218307, 218309, 218311, 218313, 218315 };

	public _39515UntruthUpset() {
		super(39515);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205884).addOnTalkEvent(questId);
		qe.registerQuestNpc(701154).addOnTalkEvent(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (targetId == 0) {
			switch (dialogActionId) {
				case QUEST_ACCEPT_1:
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				default:
					return closeDialogWindow(env);
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 701154) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 1352);
					case SETPRO1:
						if (env.getVisibleObject() instanceof Npc) {
							targetId = ((Npc) env.getVisibleObject()).getNpcId();
							Npc npc = (Npc) env.getVisibleObject();
							npc.getController().delete();
						}
						return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 205884) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						changeQuestStep(env, 1, 1, true);
						return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205884) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (Rnd.chance() < 20) {
				Npc npc = (Npc) env.getVisibleObject();
				npc.getController().delete();
				spawnForFiveMinutes(701154, npc.getPosition());
				return true;
			}
		}
		return false;
	}
}
