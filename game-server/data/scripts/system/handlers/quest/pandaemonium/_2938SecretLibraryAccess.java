package quest.pandaemonium;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author VladimirZ
 */
public class _2938SecretLibraryAccess extends AbstractQuestHandler {

	private final static int[] npc_ids = { 204267, 203557 }; // Oubliette and Suthran

	public _2938SecretLibraryAccess() {
		super(2938);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204267).addOnQuestStart(questId);
		for (int npc_id : npc_ids) {
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		}
	}

	private boolean areAltgardQuestsFinished(Player player) {
		int id = player.getQuestStateList().getQuestState(2200) != null ? 2022 : 24016; // 2022 Old path, 24016 New path
		QuestState qs = player.getQuestStateList().getQuestState(id);// last quest in Altgard state
		return qs != null && qs.getStatus() == QuestStatus.COMPLETE;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (targetId == 204267) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			} else if (qs.getStatus() == QuestStatus.REWARD && qs.getQuestVarById(0) == 0) {
				if (env.getDialogActionId() == USE_OBJECT && qs.getStatus() == QuestStatus.REWARD)
					return sendQuestDialog(env, 10002);
				else if (env.getDialogActionId() == SELECTED_QUEST_NOREWARD) {
					removeQuestItem(env, 182207026, 1);
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					return sendQuestEndDialog(env);
				} else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
					return sendQuestEndDialog(env);
				}
			} else if (qs.getStatus() == QuestStatus.COMPLETE) {
				TeleportService.teleportTo(player, WorldMapType.PANDAEMONIUM.getId(), 1403.2f, 1063.7f, 206.0f, (byte) 89, TeleportAnimation.FADE_OUT_BEAM);
				return true;
			}
		} else if (targetId == 203557) {
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					if (areAltgardQuestsFinished(player)) {
						return sendQuestDialog(env, 1011);
					} else
						return sendQuestDialog(env, 1097);
				} else if (env.getDialogActionId() == SET_SUCCEED) {
					if (giveQuestItem(env, 182207026, 1)) {
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
					}
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
					return true;
				} else
					return sendQuestStartDialog(env);
			}
		}
		return false;
	}
}
