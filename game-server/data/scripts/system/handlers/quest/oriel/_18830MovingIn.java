package quest.oriel;

import static com.aionemu.gameserver.model.DialogAction.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 */
public class _18830MovingIn extends AbstractQuestHandler {

	private static final Set<Integer> butlers;

	static {
		butlers = new HashSet<>();
		butlers.add(810017);
		butlers.add(810018);
		butlers.add(810019);
		butlers.add(810020);
		butlers.add(810021);
	}

	public _18830MovingIn() {
		super(18830);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(830584).addOnQuestStart(questId);

		Iterator<Integer> iter = butlers.iterator();
		while (iter.hasNext()) {
			int butlerId = iter.next();
			qe.registerQuestNpc(butlerId).addOnTalkEvent(questId);
		}
		qe.registerQuestNpc(830645).addOnTalkEvent(questId);
		qe.registerQuestHouseItem(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		House house = player.getActiveHouse();

		if (house == null) {
			return false;
		}

		if (qs == null || qs.isStartable()) {
			if (targetId == 830584) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
					case QUEST_REFUSE_1:
					case QUEST_REFUSE_SIMPLE:
						return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (butlers.contains(targetId) && qs.getQuestVarById(0) == 0) {
				if (house.getButler().getNpcId() != targetId)
					return false;
				switch (dialogActionId) {
					case USE_OBJECT:
						return sendQuestDialog(env, 1352);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
				}
			} else if (targetId == 830645) {
				if (dialogActionId == QUEST_SELECT) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_QUEST_ANOTHER_SINGLE_STEP_NOT_COMPLETED());
					return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD && targetId == 830645) {
			switch (dialogActionId) {
				case USE_OBJECT:
					return sendQuestDialog(env, 2375);
				case SELECT_QUEST_REWARD:
					return sendQuestDialog(env, 5);
				case SELECTED_QUEST_NOREWARD:
					sendQuestEndDialog(env);
					return true;
			}
		}

		return false;
	}

	@Override
	public boolean onHouseItemUseEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {
			changeQuestStep(env, 1, 1, true);
		}
		return false;
	}

}
