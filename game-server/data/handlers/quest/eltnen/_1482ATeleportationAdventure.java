package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Balthazar
 */
public class _1482ATeleportationAdventure extends AbstractQuestHandler {

	public _1482ATeleportationAdventure() {
		super(1482);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203919).addOnQuestStart(questId);
		qe.registerQuestNpc(203919).addOnTalkEvent(questId);
		qe.registerQuestNpc(203337).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203919) {
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
				case 203337:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							switch (qs.getQuestVarById(0)) {
								case 0: {
									return sendQuestDialog(env, 1011);
								}
								case 1: {
									return sendQuestDialog(env, 1352);
								}
								case 2: {
									return sendQuestDialog(env, 1693);
								}
								case 3: {
									return sendQuestDialog(env, 10002);
								}
							}
							return false;
						}
						case SETPRO1: {
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
						case SETPRO3: {
							qs.setQuestVar(3);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							TeleportService.teleportTo(player, 220020000, 1, 638, 2337, 425, (byte) 20);
							return true;
						}
						case CHECK_USER_HAS_QUEST_ITEM: {
							long itemCount1 = player.getInventory().getItemCountByItemId(182201399);
							if (itemCount1 >= 3) {
								removeQuestItem(env, 182201399, itemCount1);
								changeQuestStep(env, 1, 2);
								return sendQuestDialog(env, 10000);
							} else
								return sendQuestDialog(env, 10001);
						}
						default:
							return sendQuestStartDialog(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203337) {
				if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
