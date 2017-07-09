package quest.sanctum;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 */
public class _3968PalentinesRequest extends AbstractQuestHandler {

	public _3968PalentinesRequest() {
		super(3968);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798390).addOnQuestStart(questId);
		qe.registerQuestNpc(798176).addOnTalkEvent(questId);
		qe.registerQuestNpc(204528).addOnTalkEvent(questId);
		qe.registerQuestNpc(203927).addOnTalkEvent(questId);
		qe.registerQuestNpc(798390).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 798390) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (targetId == 798176) {
			if (qs.getStatus() == QuestStatus.START && var == 0) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (env.getDialogActionId() == SETPRO1) {
					if (giveQuestItem(env, 182206123, 1)) {
						qs.setQuestVar(++var);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					}
					return true;
				} else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 204528) {
			if (qs.getStatus() == QuestStatus.START && var == 1) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (env.getDialogActionId() == SETPRO2) {
					if (giveQuestItem(env, 182206124, 1)) {
						qs.setQuestVar(++var);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					}
					return true;
				} else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 203927) {
			if (qs.getStatus() == QuestStatus.START && var == 2) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2034);
				else if (env.getDialogActionId() == SETPRO3) {
					if (giveQuestItem(env, 182206125, 1)) {
						qs.setQuestVar(++var);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					}
					return true;
				} else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 798390) {
			if (env.getDialogActionId() == USE_OBJECT && qs.getStatus() == QuestStatus.REWARD)
				return sendQuestDialog(env, 2375);
			else if (env.getDialogActionId() == SELECT_QUEST_REWARD && qs.getStatus() != QuestStatus.COMPLETE) {
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				removeQuestItem(env, 182206123, 1);
				removeQuestItem(env, 182206124, 1);
				removeQuestItem(env, 182206125, 1);
				return sendQuestEndDialog(env);
			} else
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
