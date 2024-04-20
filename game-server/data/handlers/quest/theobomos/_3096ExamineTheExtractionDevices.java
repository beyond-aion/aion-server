package quest.theobomos;

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
public class _3096ExamineTheExtractionDevices extends AbstractQuestHandler {

	public _3096ExamineTheExtractionDevices() {
		super(3096);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798225).addOnQuestStart(questId);
		qe.registerQuestNpc(798225).addOnTalkEvent(questId);
		qe.registerQuestNpc(700423).addOnTalkEvent(questId);
		qe.registerQuestNpc(700424).addOnTalkEvent(questId);
		qe.registerQuestNpc(700425).addOnTalkEvent(questId);
		qe.registerQuestNpc(700426).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798225) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					default:
						return sendQuestStartDialog(env);
				}
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 798225:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 2375);
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							return checkQuestItems(env, 0, 1, true, 5, 2716);
						}
					}
					return false;
				case 700423:
					switch (env.getDialogActionId()) {
						case USE_OBJECT: {
							if (player.getInventory().getItemCountByItemId(182208067) < 1) {
								return true;
							}
						}
					}
					return false;
				case 700424:
					switch (env.getDialogActionId()) {
						case USE_OBJECT: {
							if (player.getInventory().getItemCountByItemId(182208068) < 1) {
								return true;
							}
						}
					}
					return false;
				case 700425:
					switch (env.getDialogActionId()) {
						case USE_OBJECT: {
							if (player.getInventory().getItemCountByItemId(182208069) < 1) {
								return true;
							}
						}
					}
					return false;
				case 700426:
					switch (env.getDialogActionId()) {
						case USE_OBJECT: {
							if (player.getInventory().getItemCountByItemId(182208070) < 1) {
								return true;
							}
						}
					}
					return false;
			}
			return false;
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798225) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
