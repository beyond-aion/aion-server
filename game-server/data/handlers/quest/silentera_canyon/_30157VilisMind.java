package quest.silentera_canyon;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 */
public class _30157VilisMind extends AbstractQuestHandler {

	public _30157VilisMind() {
		super(30157);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204304).addOnQuestStart(questId); // Vili
		qe.registerQuestNpc(204304).addOnTalkEvent(questId); // Vili
		qe.registerQuestNpc(799234).addOnTalkEvent(questId); // Nep
		qe.registerQuestNpc(700570).addOnTalkEvent(questId); // Statue Sinigalla
		qe.registerQuestNpc(799339).addOnTalkEvent(questId); // Sinigalla
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204304) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (dialogActionId == QUEST_ACCEPT_1) {
					if (!giveQuestItem(env, 182209254, 1))
						return true;
					return sendQuestStartDialog(env);
				} else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799234) {
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						return sendQuestEndDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 700570) {
				switch (dialogActionId) {
					case USE_OBJECT:
						if (var == 0) {
							spawnForFiveMinutes(799339, player.getWorldMapInstance(), (float) 545.3877, (float) 1232.0298, (float) 304.3357, (byte) 76);
							return useQuestObject(env, 0, 0, false, 0, 0, 0, 182209254, 1);
						}
				}
			}
			if (targetId == 799339) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1352);
						return false;
					case SETPRO1:
						if (var == 0) {
							defaultCloseDialog(env, 0, 0, true, false);
							final Npc npc = (Npc) env.getVisibleObject();
							ThreadPoolManager.getInstance().schedule(new Runnable() {

								@Override
								public void run() {
									npc.getController().delete();
								}
							}, 40000);
							return true;
						}
				}
			}
		}
		return false;
	}
}
