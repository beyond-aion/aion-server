package quest.altgard;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Ritsu Modified Majka
 */
public class _2239MalodorAntidote extends QuestHandler {

	private final static int questId = 2239;

	public _2239MalodorAntidote() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203613).addOnQuestStart(questId); // Gilungk
		qe.registerQuestNpc(203613).addOnTalkEvent(questId);// Gilungk
		qe.registerQuestNpc(203630).addOnTalkEvent(questId); // Vovetirn
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc) {
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		}
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203613) { // Gilungk
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 203630) { // Vovetirn
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 0) {
							return sendQuestDialog(env, 1352);
						}
						if (var == 1) {
							return sendQuestDialog(env, 1693);
						}
						return false;
					case SETPRO1:
						if (var == 0) {
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
						return false;
					case CHECK_USER_HAS_QUEST_ITEM:
						if (var == 1) {
							if (QuestService.collectItemCheck(env, true)) {
								if (!giveQuestItem(env, 182203227, 1)) { // Ampha antidote
									return true;
								}
								qs.setQuestVarById(0, qs.getQuestVarById(0) + 2);
								updateQuestStatus(env);
								return sendQuestDialog(env, 1779);
							} else {
								return sendQuestDialog(env, 1694);
							}
						}
						break;
					case SETPRO2:
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
				}
			}
			if (targetId == 203613) { // Gilungk
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 3) {
							return sendQuestDialog(env, 2034);
						}
						return false;
					case SETPRO3:
						if (var == 3) {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestDialog(env, 5);
						}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203613) { // Gilungk
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
