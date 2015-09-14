package quest.black_cloud_traders;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Cheatkiller
 */
public class _39515UntruthUpset extends QuestHandler {

	private final static int questId = 39515;
	private int[] mobs = { 218307, 218309, 218311, 218313, 218315 };

	public _39515UntruthUpset() {
		super(questId);
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
		DialogAction dialog = env.getDialog();

		if (targetId == 0) {
			switch (dialog) {
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
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 1352);
					}
					case SETPRO1: {
						if (env.getVisibleObject() instanceof Npc) {
							targetId = ((Npc) env.getVisibleObject()).getNpcId();
							Npc npc = (Npc) env.getVisibleObject();
							npc.getController().onDelete();
						}
						return defaultCloseDialog(env, 0, 1);
					}
				}
			} else if (targetId == 205884) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 2375);
					}
					case SELECT_QUEST_REWARD: {
						changeQuestStep(env, 1, 1, true);
						return sendQuestDialog(env, 5);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205884) {
				if (dialog == DialogAction.USE_OBJECT) {
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
			if (Rnd.get(1, 100) < 20) {
				Npc npc = (Npc) env.getVisibleObject();
				npc.getController().onDelete();
				QuestService.addNewSpawn(npc.getWorldId(), npc.getInstanceId(), 701154, npc.getX(), npc.getY(), npc.getZ(), (byte) 0);
				return true;
			}
		}
		return false;
	}
}
