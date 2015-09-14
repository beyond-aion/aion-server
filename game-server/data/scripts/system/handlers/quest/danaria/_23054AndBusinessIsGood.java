package quest.danaria;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xXMashUpXx corrected by Evil_dnk
 */
public class _23054AndBusinessIsGood extends QuestHandler {

	private final static int questId = 23054;

	public _23054AndBusinessIsGood() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(801127).addOnQuestStart(questId); // Kisping.
		qe.registerQuestNpc(801125).addOnTalkEvent(questId); // Kisping.
		qe.registerQuestNpc(801127).addOnTalkEvent(questId); // Ivolk.
		qe.registerQuestNpc(801128).addOnTalkEvent(questId); // Melkorka.
		qe.registerQuestItem(182213442, questId); // Mission Report.
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc) {
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		}
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 801127) { // Ivolk.
				if (env.getDialog() == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		}
		if (qs == null) {
			return false;
		}
		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 801127: { // Ivolk.
					if (env.getDialog() == DialogAction.QUEST_SELECT)
						return sendQuestDialog(env, 1693);
					if (env.getDialog() == DialogAction.SETPRO2) {
						removeQuestItem(env, 182213441, 1); // Kisping's Report.
						qs.setQuestVar(2);
						changeQuestStep(env, 1, 2, false);
						updateQuestStatus(env);
						giveQuestItem(env, 182213442, 1); // Mission Report.
						return closeDialogWindow(env);
					}
				}
			}
			switch (targetId) {
				case 801125: { // Kisping.
					switch (env.getDialog()) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1352);
						}
						case SETPRO1: {
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(env);
							giveQuestItem(env, 182213441, 1); // Kisping's Report.
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
						default:
							break;
					}
				}
				case 801128: { // Melkorka.
					switch (env.getDialog()) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 2375);
						}
						case SELECT_QUEST_REWARD: {
							qs.setQuestVar(2);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);

							removeQuestItem(env, 182213442, 1); // Mission Report.
							return sendQuestEndDialog(env);
						}
						default:
							return sendQuestEndDialog(env);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801128) { // Melkorka.
				switch (env.getDialog()) {
					case SELECT_QUEST_REWARD: {
						return sendQuestDialog(env, 5);
					}
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			return HandlerResult.fromBoolean(useQuestItem(env, item, 2, 3, false, 182213442, 1));
		}
		return HandlerResult.SUCCESS; // ??
	}
}
