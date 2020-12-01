package quest.pernon;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Rolandas, bobobear
 */
public class _28828TheManyFacetsOfFriendship extends AbstractQuestHandler {

	private static final Set<Integer> butlers;

	static {
		butlers = new HashSet<>();
		butlers.add(810022);
		butlers.add(810023);
		butlers.add(810024);
		butlers.add(810025);
		butlers.add(810026);
	}

	public _28828TheManyFacetsOfFriendship() {
		super(28828);
	}

	@Override
	public void register() {
		Iterator<Integer> iter = butlers.iterator();
		while (iter.hasNext()) {
			int butlerId = iter.next();
			qe.registerQuestNpc(butlerId).addOnQuestStart(questId);
			qe.registerQuestNpc(butlerId).addOnTalkEvent(questId);
		}
		qe.registerQuestItem(182213205, questId);
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
					return sendQuestStartDialog(env, 182213205, 1);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch (dialogActionId) {
				case USE_OBJECT:
					return sendQuestDialog(env, 2375);
				case SELECT_QUEST_REWARD:
					return sendQuestEndDialog(env, new int[] { 182213205 });
				case SELECTED_QUEST_NOREWARD:
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		if (id == 182213205) {
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				qs.setQuestVar(1);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
			}
		}
		return HandlerResult.UNKNOWN;
	}
}
