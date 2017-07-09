package quest.heiron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Balthazar
 */
public class _1540BaittheHooks extends AbstractQuestHandler {

	public _1540BaittheHooks() {
		super(1540);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204588).addOnQuestStart(questId);
		qe.registerQuestNpc(204588).addOnTalkEvent(questId);
		qe.registerQuestNpc(730189).addOnTalkEvent(questId);
		qe.registerQuestNpc(730190).addOnTalkEvent(questId);
		qe.registerQuestNpc(730191).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204588) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env, 182201822, 1);
				}
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 730189:
					switch (env.getDialogActionId()) {
						case USE_OBJECT: {
							if (player.getInventory().getItemCountByItemId(182201822) == 1) {
								return useQuestObject(env, 0, 1, false, 0); // 1
							}
						}
					}
					return false;
				case 730190:
					switch (env.getDialogActionId()) {
						case USE_OBJECT: {
							if (player.getInventory().getItemCountByItemId(182201822) == 1) {
								return useQuestObject(env, 1, 2, false, 0); // 2
							}
						}
					}
					return false;
				case 730191:
					switch (env.getDialogActionId()) {
						case USE_OBJECT: {
							if (qs.getQuestVarById(0) == 2 && player.getInventory().getItemCountByItemId(182201822) == 1) {
								qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								removeQuestItem(env, 182201822, 1);
								return true;
							}
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204588) {
				if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
