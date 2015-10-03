package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author mr.maddison
 */
public class _41502HiddenConnections extends QuestHandler {

	private final static int questId = 41502;

	public _41502HiddenConnections() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205935).addOnQuestStart(questId);
		qe.registerQuestNpc(205935).addOnTalkEvent(questId);
		qe.registerQuestNpc(701129).addOnTalkEvent(questId);
		qe.registerQuestItem(182212514, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205935) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 4762);
					}
					case QUEST_ACCEPT_SIMPLE: {
						return sendQuestStartDialog(env, 182212514, 1);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 701129) {
				TeleportService2.teleportTo(player, 600030000, 329, 541, 362, (byte) 100, TeleportAnimation.FADE_OUT_BEAM);
				changeQuestStep(env, 1, 2, true);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205935) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;

	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.isInsideZone(ZoneName.get("SMALL_CRATER_1_600030000")) && item.getItemId() == 182212514) {
				TeleportService2.teleportTo(player, 600030000, 604, 1018, 210, (byte) 30, TeleportAnimation.FADE_OUT_BEAM);
				changeQuestStep(env, 0, 1, false);
			}
		}
		return HandlerResult.FAILED;
	}
}
