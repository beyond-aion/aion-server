package quest.beluslan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rhys2002
 * @Modified Majka
 */
public class _2052AnUndeadOccupation extends QuestHandler {

	private final static int questId = 2052;
	private final static int[] npc_ids = { 204715, 204801, 204805 };// 182204303 184000022 152000553 182204304

	public _2052AnUndeadOccupation() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerQuestItem(182204304, questId);
		qe.registerQuestNpc(213044).addOnKillEvent(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 2500, true); // Sets as zone mission to avoid it appears on new player list.
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204715) {
				if (dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if (dialog == DialogAction.SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
			return false;
		}
		else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 204715) {
			switch (dialog) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
				   break;
				case SETPRO1:
					changeQuestStep(env, 0, 1, false);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
			}
		}
		else if (targetId == 204801) {
			switch (dialog) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1352);
					else if (var == 7)
						return sendQuestDialog(env, 1693);
				   break;
				case SETPRO2:
						changeQuestStep(env, 1, 2, false);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
				case SETPRO3:
						changeQuestStep(env, 7, 8, false);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
			}
		}
		else if (targetId == 204805) {
			switch (dialog) {
				case QUEST_SELECT:
					if (var == 8)
						return sendQuestDialog(env, 2034);
					if (var == 9)
						return sendQuestDialog(env, 2375);
				   break;
				case CHECK_USER_HAS_QUEST_ITEM:
					if (QuestService.collectItemCheck(env, true)) {
						if (!giveQuestItem(env, 182204304, 1))
							return true;
						changeQuestStep(env, 9, 10, false);
						return sendQuestDialog(env, 10000);
					}
					else
						return sendQuestDialog(env, 10001);
				case SETPRO4:
						changeQuestStep(env, 8, 9, false);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;
		return defaultOnKillEvent(env, 213044, 2, 7);
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		return HandlerResult.fromBoolean(useQuestItem(env, item, 10, 10, true, 234));
	}

}
