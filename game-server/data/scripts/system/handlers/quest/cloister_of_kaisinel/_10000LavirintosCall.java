package quest.cloister_of_kaisinel;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author dta3000
 * @modified kale, vlog
 */
public class _10000LavirintosCall extends QuestHandler {

	private final static int questId = 10000;

	public _10000LavirintosCall() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterWorld(questId);
		qe.registerQuestNpc(203701).addOnQuestStart(questId); // lavirintos
		qe.registerQuestNpc(203701).addOnTalkEvent(questId);
		qe.registerQuestNpc(798600).addOnTalkEvent(questId); // eremitia
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		if (player.getWorldId() == 110010000) {
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs == null) {
				env.setQuestId(questId);
				if (QuestService.startQuest(env)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 798600) {
			if (qs == null) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203701 && var == 0) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (env.getDialogId() == DialogAction.SET_SUCCEED.id()) {
					if (!giveQuestItem(env, 182206300, 1))
						return true;
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
					return true;
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798600) {
				if (env.getDialog() == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else {
					int[] quests = { 10001, 10026, 10020, 10021, 10022, 10023, 10024, 10025 };
					for (int quest : quests) {
						QuestEngine.getInstance().onEnterZoneMissionEnd(
							new QuestEnv(env.getVisibleObject(), env.getPlayer(), quest, env.getDialogId()));
					}
					int[] questItems = { 182206300 };
					return sendQuestEndDialog(env, questItems);
				}
			}
		}
		return false;
	}
}
