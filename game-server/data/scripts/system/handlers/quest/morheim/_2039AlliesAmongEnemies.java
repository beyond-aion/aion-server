package quest.morheim;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Hellboy aion4Free
 * @Modified Majka
 */
public class _2039AlliesAmongEnemies extends QuestHandler {

	private final static int questId = 2039;
	private final static int[] npc_ids = { 204345, 204387, 204388, 204411, 204412, 204413 };

	public _2039AlliesAmongEnemies() {
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
		return defaultOnLvlUpEvent(env, 2300, true); // Sets as zone mission to avoid it appears on new player list.
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
				case 204345: {
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
						case SETPRO1:
							if (var == 0) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
				}
					break;
				case 204387: {
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
							else if (var == 2)
								return sendQuestDialog(env, 2375);
						case SELECT_ACTION_1353:
							playQuestMovie(env, 84);
							break;
						case SETPRO2:
							if (var == 1) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
						case SET_SUCCEED:
							if (var == 2) {
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
				}
					break;
				case 204411: {
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 2)
								return sendQuestDialog(env, 1694);
						case SETPRO3:
							if (var == 2) {
								qs.setQuestVarById(1, 1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
				}
					break;
				case 204412: {
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 2)
								return sendQuestDialog(env, 1779);
						case SETPRO4:
							if (var == 2) {
								qs.setQuestVarById(2, 1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
				}
					break;
				case 204413: {
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 2)
								return sendQuestDialog(env, 1864);
						case SETPRO5:
							if (var == 2) {
								qs.setQuestVarById(3, 1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
				}
					break;
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204388) {
				if (dialog == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
