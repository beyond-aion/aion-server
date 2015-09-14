package quest.fort_tiamat;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Cheatkiller
 */
public class _10072TheTruthRevealed extends QuestHandler {

	private final static int questId = 10072;

	public _10072TheTruthRevealed() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 800365, 205535, 205579 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerOnLevelUp(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 205535) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 0) {
							return sendQuestDialog(env, 1011);
						} else if (player.getInventory().getItemCountByItemId(182213244) >= 3)
							return sendQuestDialog(env, 1352);
						else if (var == 2)
							return sendQuestDialog(env, 1693);
					}
					case CHECK_USER_HAS_QUEST_ITEM: {
						return checkQuestItems(env, 1, 2, false, 10000, 10001);
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1); // 1
					}
					case SETPRO2: {
						return sendQuestDialog(env, 1693);
					}
					case SETPRO3: {
						// teleport to israphel 800365
						removeQuestItem(env, 182213243, 1);
						WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300500000);
						InstanceService.registerPlayerWithInstance(newInstance, player);
						TeleportService2.teleportTo(player, 300500000, newInstance.getInstanceId(), 260.61f, 248.51f, 124.95f, (byte) 102,
							TeleportAnimation.BEAM_ANIMATION);
						return defaultCloseDialog(env, 2, 3);
					}
				}
			} else if (targetId == 800365) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 3) {
							return sendQuestDialog(env, 2034);
						}
					}
					case SETPRO4: {
						playQuestMovie(env, 494);
						return defaultCloseDialog(env, 3, 4);
					}
				}
			} else if (targetId == 205579) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (var == 4) {
							return sendQuestDialog(env, 2375);
						}
					}
					case SET_SUCCEED: {
						return defaultCloseDialog(env, 4, 5, true, false);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205535) {
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
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 10071);
	}
}
