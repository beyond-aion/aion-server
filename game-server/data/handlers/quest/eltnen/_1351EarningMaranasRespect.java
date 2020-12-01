package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Atomics
 */
public class _1351EarningMaranasRespect extends AbstractQuestHandler {

	public _1351EarningMaranasRespect() {
		super(1351);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203965).addOnQuestStart(questId); // Castor
		qe.registerQuestNpc(203965).addOnTalkEvent(questId); // Castor
		qe.registerQuestNpc(203983).addOnTalkEvent(questId); // Marana
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		long itemCount;
		if (targetId == 203965) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 203983) {
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogActionId() == CHECK_USER_HAS_QUEST_ITEM) {
					itemCount = player.getInventory().getItemCountByItemId(182201321);
					if (itemCount > 9) {
						removeQuestItem(env, 182201321, 10);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestDialog(env, 5);
					} else
						return sendQuestDialog(env, 2716);
				} else
					return sendQuestStartDialog(env);
			} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
