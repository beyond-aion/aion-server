package quest.pernon;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _28831justaSteptotheLeft extends QuestHandler {

	private static final int questId = 28831;
	private static final Set<Integer> butlers;

	static {
		butlers = new HashSet<>();
		butlers.add(810022);
		butlers.add(810023);
		butlers.add(810024);
		butlers.add(810025);
		butlers.add(810026);
	}

	public _28831justaSteptotheLeft() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(830651).addOnQuestStart(questId);
		Iterator<Integer> iter = butlers.iterator();
		while (iter.hasNext()) {
			int butlerId = iter.next();
			qe.registerQuestNpc(butlerId).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (targetId == 830651) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
				}
			}
		}

		else if (qs.getStatus() == QuestStatus.START && butlers.contains(targetId)) {
			House house = player.getActiveHouse();
			if (house.getButler().getNpcId() != targetId)
				return false;
			switch (dialog) {
				case USE_OBJECT:
					return sendQuestDialog(env, 2375);
				case SELECT_QUEST_REWARD:
					changeQuestStep(env, 0, 0, true);
					return sendQuestDialog(env, 5);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD && butlers.contains(targetId)) {
			return sendQuestEndDialog(env);
		}

		return false;
	}

}
