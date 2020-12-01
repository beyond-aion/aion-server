package quest.eltnen;

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
public class _3329DinnersonMe extends AbstractQuestHandler {

	private final static int[] mob_ids = { 210887, 210912, 210914, 210932 };

	public _3329DinnersonMe() {
		super(3329);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203909).addOnQuestStart(questId);
		qe.registerQuestNpc(203909).addOnTalkEvent(questId);
		qe.registerQuestNpc(203956).addOnTalkEvent(questId);
		for (int mob_id : mob_ids)
			qe.registerQuestNpc(mob_id).addOnKillEvent(questId);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		int[] mobs1 = { 210887, 210912 };
		int[] mobs2 = { 210914, 210932 };
		if (defaultOnKillEvent(env, mobs1, 0, 4, 0) || defaultOnKillEvent(env, mobs2, 0, 6, 1))
			return true;
		else
			return false;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		int dialogActionId = env.getDialogActionId();
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (targetId == 203909) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203956) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestDialog(env, 2375);
					case SELECT_QUEST_REWARD:
						return sendQuestEndDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203956) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
