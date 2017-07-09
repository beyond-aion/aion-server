package quest.oriel;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Rolandas
 */
public class _18821AlmostForgotMyBlessings extends AbstractQuestHandler {

	private static final Set<Integer> butlers;

	static {
		butlers = new HashSet<>();
		butlers.add(810017);
		butlers.add(810018);
		butlers.add(810019);
		butlers.add(810020);
		butlers.add(810021);
	}

	public _18821AlmostForgotMyBlessings() {
		super(18821);
	}

	@Override
	public void register() {
		Iterator<Integer> iter = butlers.iterator();
		while (iter.hasNext()) {
			int butlerId = iter.next();
			qe.registerQuestNpc(butlerId).addOnQuestStart(questId);
			qe.registerQuestNpc(butlerId).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();

		if (!butlers.contains(targetId))
			return false;

		House house = player.getActiveHouse();
		if (house == null || house.getButler() == null || house.getButler().getNpcId() != targetId)
			return false;

		int dialogActionId = env.getDialogActionId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			switch (dialogActionId) {
				case QUEST_SELECT:
					return sendQuestDialog(env, 1011);
				case QUEST_ACCEPT_1:
				case QUEST_ACCEPT_SIMPLE:
					return sendQuestStartDialog(env);
				case QUEST_REFUSE_1:
				case QUEST_REFUSE_SIMPLE:
					return sendQuestDialog(env, 1004);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (dialogActionId) {
				case QUEST_SELECT:
					return sendQuestDialog(env, 2375);
				case SELECT_QUEST_REWARD:
					changeQuestStep(env, 0, 0, true);
					return sendQuestDialog(env, 5);
				case SELECTED_QUEST_NOREWARD:
					return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch (dialogActionId) {
				case USE_OBJECT:
					return sendQuestDialog(env, 5);
				case SELECTED_QUEST_NOREWARD:
					sendQuestEndDialog(env);
					return true;
			}
		}

		return false;
	}

}
