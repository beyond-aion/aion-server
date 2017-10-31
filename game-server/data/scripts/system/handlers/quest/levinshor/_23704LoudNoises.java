package quest.levinshor;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Pad
 */
public class _23704LoudNoises extends AbstractQuestHandler {

	private static final int npcId = 802343; // Rink
	private static final int itemId = 182215533;

	public _23704LoudNoises() {
		super(23704);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(npcId).addOnQuestStart(questId);
		qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		qe.registerQuestItem(itemId, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == npcId) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env, itemId, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == npcId) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 1) {
							return sendQuestDialog(env, 2375);
						}
						return false;
					case SELECT_QUEST_REWARD:
						return defaultCloseDialog(env, 1, 1, true, true);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == npcId) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.isInsideZone(ZoneName.get("LDF4_ADVANCE_ITEMUSEAREA_Q23704_600100000"))) {
				return HandlerResult.fromBoolean(useQuestItem(env, item, 0, 1, false));
			}
		}
		return HandlerResult.FAILED;
	}

}
