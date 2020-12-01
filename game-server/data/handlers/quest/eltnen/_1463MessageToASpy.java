package quest.eltnen;

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
 * @author Balthazar
 */
public class _1463MessageToASpy extends AbstractQuestHandler {

	public _1463MessageToASpy() {
		super(1463);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203940).addOnQuestStart(questId);
		qe.registerQuestNpc(203940).addOnTalkEvent(questId);
		qe.registerQuestNpc(203903).addOnTalkEvent(questId);
		qe.registerQuestNpc(204424).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203940) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else
					return sendQuestStartDialog(env);
			}
		}
		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203903:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							if (qs.getQuestVarById(0) == 0) {
								return sendQuestDialog(env, 1352);
							} else if (qs.getQuestVarById(0) == 2) {
								return sendQuestDialog(env, 2375);
							}
							return false;
						}
						case SETPRO1: {
							if (player.getInventory().getItemCountByItemId(182201382) == 0)
								if (!giveQuestItem(env, 182201382, 1))
									return true;
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
						case SELECT_QUEST_REWARD: {
							qs.setQuestVar(3);
							removeQuestItem(env, 182201383, 1);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestEndDialog(env);
						}
					}
					return false;
				case 204424:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							if (qs.getQuestVarById(0) == 1) {
								return sendQuestDialog(env, 1693);
							}
							return false;
						}
						case SETPRO2: {
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							removeQuestItem(env, 182201382, 1);
							if (player.getInventory().getItemCountByItemId(182201383) == 0)
								if (!giveQuestItem(env, 182201383, 1))
									return true;
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
						default:
							return sendQuestEndDialog(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203903) {
				if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
