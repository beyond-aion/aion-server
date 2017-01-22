package quest.morheim;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Cheatkiller
 */
public class _2343AncientGinseng extends QuestHandler {

	private final static int questId = 2343;

	public _2343AncientGinseng() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182204134, questId);
		qe.registerQuestNpc(700243).addOnTalkEvent(questId);
		qe.registerCanAct(questId, 700243);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 0) {
				if (dialog == DialogAction.QUEST_ACCEPT_1) {
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 700243) {
				Npc npc = player.getPosition().getWorldMapInstance().getNpc(212814);
				if (npc == null)
					QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 212814, 1008.36f, 481.88f, 509.3f, (byte) 60);
				removeQuestItem(env, 182204134, 1);
				qs.setStatus(QuestStatus.REWARD);
				QuestService.finishQuest(env, 0);
				return true;
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
		}
		return HandlerResult.FAILED;
	}
}
