package quest.inggison;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Luzien, Neon
 */
public class _11118MakingSetzkikiLaugh extends AbstractQuestHandler {

	private final static int[] npc_ids = { 798985, 798963, 798986 };

	public _11118MakingSetzkikiLaugh() {
		super(11118);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798985).addOnQuestStart(questId);
		for (int npc_id : npc_ids) {
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		}
		qe.registerQuestItem(182206795, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		if (sendQuestNoneDialog(env, 798985, 4762))
			return true;
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int targetId = env.getTargetId();
		int var = qs.getQuestVarById(0);
		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798963) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1011);
						else if (var == 1)
							return sendQuestDialog(env, 1352);
						return false;
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1);
					case CHECK_USER_HAS_QUEST_ITEM:
						return checkQuestItems(env, 1, 2, false, 10000, 10001, 182206795, 1);
				}
			} else if (targetId == 798986) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 3)
							return sendQuestDialog(env, 2034);
						return false;
					case SET_SUCCEED:
						return defaultCloseDialog(env, 3, 4, true, false);
				}
			}
		}
		return sendQuestRewardDialog(env, 798985, 10002);
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				if (player.isInsideItemUseZone(item.getItemTemplate().getUseArea())) {
					return HandlerResult.fromBoolean(useQuestItem(env, item, 2, 3, false));
				}
			}
		}
		return HandlerResult.FAILED;
	}
}
