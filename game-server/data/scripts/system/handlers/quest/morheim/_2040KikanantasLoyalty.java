package quest.morheim;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Hellboy aion4Free
 * @Modified Majka
 */
public class _2040KikanantasLoyalty extends QuestHandler {

	private final static int questId = 2040;
	private final static int[] npc_ids = { 204388, 204414, 204304, 204345 };

	public _2040KikanantasLoyalty() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		int[] quests = { 2300, 2039 };
		return defaultOnLvlUpEvent(env, quests, true); // Sets as zone mission to avoid it appears on new player list.
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204388: {
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							else if (var == 3)
								return sendQuestDialog(env, 2034);
						case SETPRO1:
							if (var == 0) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
						case SETPRO4:
							if (var == 3) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
				}
					break;
				case 204345: {
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 4)
								return sendQuestDialog(env, 2375);
						case SET_SUCCEED:
							if (var == 4) {
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
				}
					break;
				case 204414: {
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
							else if (var == 2)
								return sendQuestDialog(env, 1693);
						case SELECT_ACTION_1354:
							playQuestMovie(env, 85);
							break;
						case SETPRO2:
							if (var == 1) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
						case SETPRO3:
							if (var == 2) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
						case CHECK_USER_HAS_QUEST_ITEM:
							if (var == 2) {
								if (QuestService.collectItemCheck(env, false)) {
									return sendQuestDialog(env, 10000);
								}
								else
									return sendQuestDialog(env, 10001);
							}
					}
				}
					break;
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204304) {
				if (dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else {
					removeQuestItem(env, 182204018, 1);
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
