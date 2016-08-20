package quest.theobomos;

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
 * @author Balthazar
 */

public class _3041SororunerkWhereabouts extends QuestHandler {

	private final static int questId = 3041;

	public _3041SororunerkWhereabouts() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798167).addOnQuestStart(questId);
		qe.registerQuestNpc(798167).addOnTalkEvent(questId);
		qe.registerQuestNpc(700378).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 798167) {
				switch (env.getDialog()) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1011);
					}
					default:
						return sendQuestStartDialog(env);
				}
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 700378: {
					switch (env.getDialog()) {
						case USE_OBJECT: {
							if (qs.getQuestVarById(0) == 0) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						}
						case SETPRO1: {
							if (player.getInventory().getItemCountByItemId(182208031) == 0) {
								if (!giveQuestItem(env, 182208031, 1)) {
									return true;
								}
							}
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
							return true;
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798167) {
				if (env.getDialogId() == DialogAction.SELECT_QUEST_REWARD.id())
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
