package quest.oriel;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _18806HeartofRock extends AbstractQuestHandler {


	private static final Set<Integer> butlers;

	static {
		butlers = new HashSet<>();
		butlers.add(810017);
		butlers.add(810018);
		butlers.add(810019);
		butlers.add(810020);
		butlers.add(810021);
	}

	public _18806HeartofRock() {
		super(18806);
	}

	@Override
	public void register() {
		Iterator<Integer> iter = butlers.iterator();
		while (iter.hasNext()) {
			int butlerId = iter.next();
			qe.registerQuestNpc(butlerId).addOnQuestStart(questId);
			qe.registerQuestNpc(butlerId).addOnTalkEvent(questId);
		}
		qe.registerQuestNpc(830528).addOnTalkEvent(questId);
		qe.registerQuestNpc(830194).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (butlers.contains(targetId)) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 830528:
					switch (dialogActionId) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1352);
						}
						case SELECT2_1: {
							return sendQuestDialog(env, 1353);
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1);
						}
					}
					break;
				case 830194:
					switch (dialogActionId) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 2375);
						}
						case SELECT_QUEST_REWARD:
							changeQuestStep(env, 1, 1, true);
							return sendQuestDialog(env, 5);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 830194) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
