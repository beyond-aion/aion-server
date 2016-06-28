package quest.heiron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Balthazar
 * @rework Cheatkiller
 */

public class _1640TeleporterRepairs extends QuestHandler {

	private final static int questId = 1640;

	public _1640TeleporterRepairs() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(730033).addOnQuestStart(questId);
		qe.registerQuestNpc(730033).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
			if (targetId == 730033) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else if (dialog == DialogAction.SETPRO1) {
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 730033) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1352);
					}
					case SETPRO2: {
						if (player.getInventory().getItemCountByItemId(182201790) > 0) {
							removeQuestItem(env, 182201790, 1);
							qs.setStatus(QuestStatus.REWARD);
							QuestService.finishQuest(env, 0);
							TeleportService2.teleportTo(player, WorldMapType.HEIRON.getId(), 187.71689f, 2712.14870f, 141.91672f, (byte) 195,
								TeleportAnimation.FADE_OUT_BEAM);
							return closeDialogWindow(env);
						} else
							return sendQuestDialog(env, 1353);
					}
				}
			}
		}
		return false;
	}
}
