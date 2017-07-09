package quest.sanctum;

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
 * @author xaerolt, Rolandas
 */
public class _1926SecretLibraryAccess extends AbstractQuestHandler {

	private final static int[] npc_ids = { 203894, 203701 };// Latri and Lavirintos

	public _1926SecretLibraryAccess() {
		super(1926);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203894).addOnQuestStart(questId);
		for (int npc_id : npc_ids) {
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		}
	}

	private boolean AreVerteronQuestsFinished(Player player) {
		int id = player.getQuestStateList().getQuestState(1130) != null ? 1020 : 14016; // 1020 Old path, 14016 New path
		QuestState qs = player.getQuestStateList().getQuestState(id);// last quest in Verteron state
		return qs != null && qs.getStatus() == QuestStatus.COMPLETE;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (targetId == 203894) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			} else if (qs.getStatus() == QuestStatus.REWARD && qs.getQuestVarById(0) == 0 || qs.getStatus() == QuestStatus.COMPLETE) {
				if (env.getDialogActionId() == USE_OBJECT && qs.getStatus() == QuestStatus.REWARD)
					return sendQuestDialog(env, 10002);
				else if (env.getDialogActionId() == SELECTED_QUEST_NOREWARD) {
					removeQuestItem(env, 182206022, 1);
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
					return sendQuestEndDialog(env);
				} else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
					return sendQuestEndDialog(env);
				}
				TeleportService.teleportTo(player, WorldMapType.SANCTUM.getId(), 2032.9f, 1473.1f, 592.2f, (byte) 119, TeleportAnimation.FADE_OUT_BEAM);
				return true;
			}
		} else if (targetId == 203701) {
			if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					if (AreVerteronQuestsFinished(player)) {
						return sendQuestDialog(env, 1011);
					} else
						return sendQuestDialog(env, 1097);
				} else if (env.getDialogActionId() == SET_SUCCEED) {
					if (giveQuestItem(env, 182206022, 1)) {
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
