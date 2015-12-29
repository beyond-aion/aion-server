package quest.daevanion;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Nephis
 * @modified Gigi, Pad
 */
public class _2988TheWiseInDisguise extends QuestHandler {

	private final static int questId = 2988;

	public _2988TheWiseInDisguise() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204182).addOnQuestStart(questId);// Heimdall
		qe.registerQuestNpc(204182).addOnTalkEvent(questId);// Heimdall
		qe.registerQuestNpc(204338).addOnTalkEvent(questId);// Utgar
		qe.registerQuestNpc(204213).addOnTalkEvent(questId);// Brakan
		qe.registerQuestNpc(204146).addOnTalkEvent(questId);// Kanensa
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 204182) { // Heimdall
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (targetId == 204338) { // Utgar
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (env.getDialog() == DialogAction.SETPRO1)
					return defaultCloseDialog(env, 0, 1);
			}
		} else if (targetId == 204213) { // Brakan
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (env.getDialog() == DialogAction.SETPRO2)
					return defaultCloseDialog(env, 1, 2);
			}
		} else if (targetId == 204146) { // Kanensa
			if (qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 2) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 2034);
				else if (env.getDialog() == DialogAction.SELECT_ACTION_2035)
					return checkQuestItems(env, 2, 2, true, 2035, 2120);
			} else if (qs.getStatus() == QuestStatus.REWARD) {
				return sendQuestEndDialog(env);
			}
		}

		return false;
	}
}
