package quest.heiron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Report-To-Quest Start: Perento (204500) Take the paper voucher (182213000) to Koruchinerk (798321) Go to New Heiron Gate and meet Herthia (205228)
 * Bring the Fake Stigma (182213001) to Perento
 * 
 * @author vlog, Gigi
 */
public class _18600ScoringSomeBadStigma extends AbstractQuestHandler {

	private final static int _questId = 18600;
	private final static int[] _npcs = { 204500, 205228, 804601 };

	public _18600ScoringSomeBadStigma() {
		super(_questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204500).addOnQuestStart(_questId);
		for (int npc_id : _npcs)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(_questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(_questId);

		if (targetId == 204500) { // Perento
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (env.getDialogActionId() == QUEST_ACCEPT_1)
					return sendQuestStartDialog(env, 182213000, 1);
				else
					return sendQuestStartDialog(env, 182213000, 1);
			} else if (qs.getStatus() == QuestStatus.REWARD) {
				if (env.getDialogActionId() == USE_OBJECT)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
					removeQuestItem(env, 182213001, 1);
					return sendQuestEndDialog(env);
				} else
					return sendQuestEndDialog(env);
			}
		}
		if (targetId == 804601) { // Koruchinerk
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				if (env.getDialogActionId() == SETPRO1)
					return defaultCloseDialog(env, 0, 1, 0, 0, 182213000, 1); // 1
			}
		}
		if (targetId == 205228) { // Herthia
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (env.getDialogActionId() == SETPRO2) {
					qs.setQuestVar(3);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					giveQuestItem(env, 182213001, 1);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			}
		}
		return false;
	}
}
