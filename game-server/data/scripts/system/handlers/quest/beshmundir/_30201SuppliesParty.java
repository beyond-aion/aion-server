package quest.beshmundir;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi
 */
public class _30201SuppliesParty extends AbstractQuestHandler {

	public _30201SuppliesParty() {
		super(30201);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798926).addOnQuestStart(questId);
		qe.registerQuestNpc(798926).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		if (targetId == 798926) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			} else if (qs.getStatus() == QuestStatus.START) {
				long itemCount;
				if (env.getDialogActionId() == QUEST_SELECT && qs.getQuestVarById(0) == 0)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogActionId() == CHECK_USER_HAS_QUEST_ITEM && qs.getQuestVarById(0) == 0) {
					itemCount = player.getInventory().getItemCountByItemId(182209601);
					if (itemCount > 0) {
						removeQuestItem(env, 182209601, 1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestDialog(env, 5);
					} else
						return sendQuestDialog(env, 2716);
				} else
					return sendQuestEndDialog(env);
			} else
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
