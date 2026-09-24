package quest.esoterrace;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Vincas
 */
public class _28405KexkrasPast extends AbstractQuestHandler {

	private static final int npcLuigur = 799558, npcRelyt = 799557;

	public _28405KexkrasPast() {
		super(28405);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(npcLuigur).addOnTalkEvent(questId);
		qe.registerQuestNpc(npcRelyt).addOnTalkEvent(questId);
		qe.registerQuestItem(182215014, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();

		if (env.getTargetId() == 0 && env.getDialogActionId() == QUEST_ACCEPT_1) {
			QuestService.startQuest(env);
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
			return true;
		}

		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (env.getTargetId()) {
				case npcLuigur:
					if (qs.getQuestVarById(0) == 0) {
						if (env.getDialogActionId() == QUEST_SELECT)
							return sendQuestDialog(env, 1352);
						else if (env.getDialogActionId() == SETPRO1)
							return defaultCloseDialog(env, 0, 1, 182215025, 1, 182215014, 1);
					}
					return false;
				case npcRelyt:
					if (qs.getQuestVarById(0) == 1) {
						if (env.getDialogActionId() == QUEST_SELECT)
							return sendQuestDialog(env, 2375);
						else if (env.getDialogActionId() == SELECT_QUEST_REWARD)
							removeQuestItem(env, 182215025, 1);
						return defaultCloseDialog(env, 1, 2, true, true);
					}
			}
		}
		return sendQuestRewardDialog(env, npcRelyt, 0);
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		int id = item.getItemTemplate().getTemplateId();

		if (id != 182215014)
			return HandlerResult.FAILED;
		sendQuestDialog(env, 4);
		return HandlerResult.SUCCESS;
	}
}
