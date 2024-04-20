package quest.verteron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Balthazar, Pad
 */
public class _1162AltenosWeddingRing extends AbstractQuestHandler {

	public _1162AltenosWeddingRing() {
		super(1162);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203095).addOnQuestStart(questId);
		qe.registerQuestNpc(203095).addOnTalkEvent(questId);
		qe.registerQuestNpc(203093).addOnTalkEvent(questId);
		qe.registerQuestNpc(700005).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (targetId == 203095) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 700005:
					if (qs.getQuestVarById(0) == 0) {
						switch (env.getDialogActionId()) {
							case USE_OBJECT: {
								return sendQuestDialog(env, 3739);
							}
							case SETPRO1: {
								if (player.getInventory().getItemCountByItemId(182200563) == 0) {
									if (!giveQuestItem(env, 182200563, 1))
										return true;
								}
								return defaultCloseDialog(env, 0, 1);
							}
						}
					}
					return false;
				case 203093:
					if (qs.getQuestVarById(0) == 1) {
						switch (env.getDialogActionId()) {
							case USE_OBJECT:
								return sendQuestDialog(env, 2034);
							case CHECK_USER_HAS_QUEST_ITEM:
								qs.setRewardGroup(1);
								return checkQuestItems(env, 1, 1, true, 6, 2375);
							case SETPRO2:
								return sendQuestDialog(env, 2375);
						}
					}
					return false;
				case 203095:
					if (qs.getQuestVarById(0) == 1) {
						switch (env.getDialogActionId()) {
							case USE_OBJECT:
								return sendQuestDialog(env, 1352);
							case CHECK_USER_HAS_QUEST_ITEM:
								qs.setRewardGroup(0);
								return checkQuestItems(env, 1, 1, true, 5, 1693);
							case SETPRO2:
								return sendQuestDialog(env, 1693);
						}
					}
					return false;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203093) {
				if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 6);
				else
					return sendQuestEndDialog(env);
			} else if (targetId == 203095) {
				if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
