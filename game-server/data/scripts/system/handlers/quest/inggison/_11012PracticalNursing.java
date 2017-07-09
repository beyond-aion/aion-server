package quest.inggison;

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
 * @author dta3000
 */
public class _11012PracticalNursing extends AbstractQuestHandler {

	public _11012PracticalNursing() {
		super(11012);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799071).addOnQuestStart(questId);
		qe.registerQuestNpc(799071).addOnTalkEvent(questId);
		qe.registerQuestNpc(799072).addOnTalkEvent(questId);
		qe.registerQuestNpc(799073).addOnTalkEvent(questId);
		qe.registerQuestNpc(799074).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 799071) {
			if (qs == null) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else if (env.getDialogActionId() == QUEST_ACCEPT_1) {
					if (giveQuestItem(env, 182206715, 3))
						return sendQuestStartDialog(env);
					else
						return true;
				} else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799072 && var == 0) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else if (env.getDialogActionId() == SETPRO1) {
					qs.setQuestVar(++var);
					updateQuestStatus(env);
					removeQuestItem(env, 182206715, 1);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				} else
					return sendQuestStartDialog(env);
			} else if (targetId == 799073 && var == 1) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1352);
				} else if (env.getDialogActionId() == SETPRO2) {
					qs.setQuestVar(++var);
					updateQuestStatus(env);
					removeQuestItem(env, 182206715, 1);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				} else
					return sendQuestStartDialog(env);
			} else if (targetId == 799074 && var == 2) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1693);
				} else if (env.getDialogActionId() == SETPRO3) {
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					removeQuestItem(env, 182206715, 1);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				} else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799071) {
				if (env.getDialogActionId() == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
