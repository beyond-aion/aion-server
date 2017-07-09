package quest.poeta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author MrPoke
 */
public class _1122DeliveringPernossRobe extends AbstractQuestHandler {

	public _1122DeliveringPernossRobe() {
		super(1122);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203060).addOnQuestStart(questId);
		qe.registerQuestNpc(203060).addOnTalkEvent(questId);
		qe.registerQuestNpc(790001).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 203060) {
			if (qs == null) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (env.getDialogActionId() == QUEST_ACCEPT_1) {
					if (giveQuestItem(env, 182200216, 1))
						return sendQuestStartDialog(env);
					else
						return true;
				} else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 790001) {
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				long itemCount;
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1352);
					case SETPRO1:
						itemCount = player.getInventory().getItemCountByItemId(182200218);
						if (itemCount > 0) {
							qs.setQuestVar(1);
							qs.setRewardGroup(0);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							removeQuestItem(env, 182200218, 1);
							removeQuestItem(env, 182200216, 1);
							return sendQuestDialog(env, 1523);
						} else
							return sendQuestDialog(env, 1608);

					case SETPRO2:
						itemCount = player.getInventory().getItemCountByItemId(182200219);
						if (itemCount > 0) {
							qs.setQuestVar(2);
							qs.setRewardGroup(1);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							removeQuestItem(env, 182200219, 1);
							removeQuestItem(env, 182200216, 1);
							return sendQuestDialog(env, 1438);
						} else
							return sendQuestDialog(env, 1608);
					case SETPRO3:
						itemCount = player.getInventory().getItemCountByItemId(182200220);
						if (itemCount > 0) {
							qs.setQuestVar(3);
							qs.setRewardGroup(2);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							removeQuestItem(env, 182200220, 1);
							removeQuestItem(env, 182200216, 1);
							return sendQuestDialog(env, 1353);
						} else
							return sendQuestDialog(env, 1608);
					default:
						return sendQuestStartDialog(env);
				}
			} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
