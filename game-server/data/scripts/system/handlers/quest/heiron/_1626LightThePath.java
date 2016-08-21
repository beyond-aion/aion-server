package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Balthazar
 */

public class _1626LightThePath extends QuestHandler {

	private final static int questId = 1626;

	public _1626LightThePath() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204592).addOnQuestStart(questId);
		qe.registerQuestNpc(204592).addOnTalkEvent(questId);
		qe.registerQuestNpc(700221).addOnTalkEvent(questId);
		qe.registerQuestNpc(700222).addOnTalkEvent(questId);
		qe.registerQuestNpc(700223).addOnTalkEvent(questId);
		qe.registerQuestNpc(700224).addOnTalkEvent(questId);
		qe.registerQuestNpc(700225).addOnTalkEvent(questId);
		qe.registerQuestNpc(700226).addOnTalkEvent(questId);
		qe.registerQuestNpc(700227).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 204592) {
				switch (env.getDialog()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 4762);
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env, 182201788, 1);
				}
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 700221:
					switch (env.getDialog()) {
						case USE_OBJECT: {
							long itemCount1 = player.getInventory().getItemCountByItemId(182201788);
							if (itemCount1 == 1) {
								return useQuestObject(env, 0, 1, false, 0); // 1
							}
						}
					}
					break;
				case 700222:
					switch (env.getDialog()) {
						case USE_OBJECT: {
							long itemCount1 = player.getInventory().getItemCountByItemId(182201788);
							if (itemCount1 == 1) {
								return useQuestObject(env, 1, 2, false, 0); // 2
							}
						}
					}
					break;
				case 700223:
					switch (env.getDialog()) {
						case USE_OBJECT: {
							long itemCount1 = player.getInventory().getItemCountByItemId(182201788);
							if (itemCount1 == 1) {
								return useQuestObject(env, 2, 3, false, 0); // 3
							}
						}
					}
					return false;
				case 700224:
					switch (env.getDialog()) {
						case USE_OBJECT: {
							long itemCount1 = player.getInventory().getItemCountByItemId(182201788);
							if (itemCount1 == 1) {
								return useQuestObject(env, 3, 4, false, 0); // 4
							}
						}
					}
					return false;
				case 700225:
					switch (env.getDialog()) {
						case USE_OBJECT: {
							long itemCount1 = player.getInventory().getItemCountByItemId(182201788);
							if (itemCount1 == 1) {
								return useQuestObject(env, 4, 5, false, 0); // 5
							}
						}
					}
					return false;
				case 700226:
					switch (env.getDialog()) {
						case USE_OBJECT: {
							long itemCount1 = player.getInventory().getItemCountByItemId(182201788);
							if (itemCount1 == 1) {
								return useQuestObject(env, 5, 6, false, 0); // 6
							}
						}
					}
					return false;
				case 700227:
					switch (env.getDialog()) {
						case USE_OBJECT: {
							long itemCount1 = player.getInventory().getItemCountByItemId(182201788);
							if (itemCount1 == 1) {
								return useQuestObject(env, 6, 6, true, 0); // reward
							}
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204592) {
				if (env.getDialogId() == DialogAction.SELECT_QUEST_REWARD.id())
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
