package quest.poeta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke
 */
public class _1111InsomniaMedicine extends AbstractQuestHandler {

	public _1111InsomniaMedicine() {
		super(1111);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203075).addOnQuestStart(questId);
		qe.registerQuestNpc(203075).addOnTalkEvent(questId);
		qe.registerQuestNpc(203061).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 203075) {
			if (qs == null) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			} else if (qs.getStatus() == QuestStatus.REWARD) {
				if (env.getDialogActionId() == USE_OBJECT) {
					if (qs.getQuestVarById(0) == 2) {
						removeQuestItem(env, 182200222, 1);
						return sendQuestDialog(env, 2375);
					} else if (qs.getQuestVarById(0) == 3) {
						qs.setRewardGroup(1);
						removeQuestItem(env, 182200221, 1);
						return sendQuestDialog(env, 2716);
					}
					return false;
				}
				return sendQuestEndDialog(env);
			}
		} else if (targetId == 203061) {
			if (env.getDialogActionId() == QUEST_SELECT) {
				if (qs.getQuestVarById(0) == 0)
					return sendQuestDialog(env, 1352);
				else if (qs.getQuestVarById(0) == 1)
					return sendQuestDialog(env, 1353);
				return false;
			} else if (env.getDialogActionId() == CHECK_USER_HAS_QUEST_ITEM) {
				if (QuestService.collectItemCheck(env, true)) {
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					return sendQuestDialog(env, 1353);
				} else
					return sendQuestDialog(env, 1693);
			} else if (env.getDialogActionId() == SETPRO1 && qs.getStatus() != QuestStatus.COMPLETE) {
				if (!giveQuestItem(env, 182200222, 1))
					return true;
				qs.setQuestVarById(0, 2);
				qs.setRewardGroup(0);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
				return true;
			} else if (env.getDialogActionId() == SETPRO2 && qs.getStatus() != QuestStatus.COMPLETE) {
				if (!giveQuestItem(env, 182200221, 1))
					return true;
				qs.setQuestVarById(0, 3);
				qs.setRewardGroup(1);
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
				return true;
			}
		}
		return false;
	}
}
