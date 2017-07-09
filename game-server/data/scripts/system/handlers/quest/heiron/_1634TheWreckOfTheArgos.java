package quest.heiron;

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
public class _1634TheWreckOfTheArgos extends AbstractQuestHandler {

	public _1634TheWreckOfTheArgos() {
		super(1634);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204547).addOnQuestStart(questId);
		qe.registerQuestNpc(204547).addOnTalkEvent(questId);
		qe.registerQuestNpc(204540).addOnTalkEvent(questId);
		qe.registerQuestNpc(790018).addOnTalkEvent(questId);
		qe.registerQuestNpc(204541).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204547) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204547:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							long itemCount1 = player.getInventory().getItemCountByItemId(182201760);
							if (qs.getQuestVarById(0) == 0 && itemCount1 >= 3) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						}
						case SELECT_NONE_1: {
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
							return true;
						}
					}
					return false;
				case 204540:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 1693);
						}
						case SELECT3_1: {
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							removeQuestItem(env, 182201760, 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
							return true;
						}
					}
					return false;
				case 790018:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							return sendQuestDialog(env, 2034);
						}
						case SELECT4_1: {
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							removeQuestItem(env, 182201760, 1);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
							return true;
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204541) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					case SELECTED_QUEST_NOREWARD:
						return sendQuestEndDialog(env, new int[] { 182201760 });
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
