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
public class _11009MeiriaFriendlySuggestion extends AbstractQuestHandler {

	public _11009MeiriaFriendlySuggestion() {
		super(11009);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798945).addOnQuestStart(questId);
		qe.registerQuestNpc(798945).addOnTalkEvent(questId);
		qe.registerQuestNpc(799008).addOnTalkEvent(questId);
		qe.registerQuestNpc(799017).addOnTalkEvent(questId);
		qe.registerQuestNpc(798941).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 798945) {
			if (qs == null) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else if (env.getDialogActionId() == QUEST_ACCEPT_1) {
					if (giveQuestItem(env, 182206711, 2))
						return sendQuestStartDialog(env);
					else
						return true;
				} else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 799008:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1352);
						}
						case SETPRO1: {
							removeQuestItem(env, 182206711, 1);
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
					}
					return false;
				case 799017:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1693);
						}
						case SETPRO2: {
							removeQuestItem(env, 182206711, 1);
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							if (giveQuestItem(env, 182206712, 1))
								return true;
						}
					}
					return false;
				case 798941:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 2034);
						}
						case SETPRO3: {
							removeQuestItem(env, 182206712, 1);
							qs.setQuestVar(3);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798945) {
				if (env.getDialogActionId() == USE_OBJECT) {
					return sendQuestDialog(env, 2375);
				} else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
