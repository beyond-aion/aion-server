package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Artur
 */
public class _14024AKrallIngSuspicion extends AbstractQuestHandler {

	public _14024AKrallIngSuspicion() {
		super(14024);
	}

	@Override
	public void register() {
		int[] npc_ids = { 203904, 204045, 204003, 204004, 204020, 203901 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 14020);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 14020);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204020) {
				removeQuestItem(env, 182201004, 1);
				qs.setRewardGroup(0); // group 0 and 1 are identical in templates, set anyway to mute warning
				return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 203904) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
					return false;
				case SETPRO1:
					if (var == 0) {
						return defaultCloseDialog(env, 0, 1);
					}
			}
		} else if (targetId == 204045) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1352);
					return false;
				case SELECT2_1_1:
					if (var == 1)
						playQuestMovie(env, 32);
					break;
				case SETPRO2:
					if (var == 1) {
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						TeleportService.teleportTo(player, 210020000, 1357f, 2566f, 279.6f, (byte) 89, TeleportAnimation.FADE_OUT_BEAM);
						return true;
					}
					return false;
			}
		} else if (targetId == 204003) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 2)
						return sendQuestDialog(env, 1693);
					else if (var == 3 && QuestService.collectItemCheck(env, true))
						return sendQuestDialog(env, 2034);
					else
						return sendQuestDialog(env, 2120);
				case SETPRO3:
					if (var == 2) {
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
					return false;
				case SETPRO4:
					if (var == 3) {
						playQuestMovie(env, 50);
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
					return false;
			}
		} else if (targetId == 204004) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 2)
						return sendQuestDialog(env, 2034);
					break;
				case CHECK_USER_HAS_QUEST_ITEM:
					if (var == 2)
						return checkQuestItems(env, 2, 2, false, 2802, 2717);
					return false;
				case SETPRO4:
					if (var == 2) {
						if (!giveQuestItem(env, 182201004, 1))
							return true;
						changeQuestStep(env, 2, 2, true);
						TeleportService.teleportTo(player, 210020000, 1608.11f, 1528.7f, 318.07f, (byte) 118, TeleportAnimation.FADE_OUT_BEAM);
						return true;
					}
			}
		}
		return false;
	}
}
