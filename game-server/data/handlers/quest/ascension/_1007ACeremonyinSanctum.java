package quest.ascension;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke + Dune11, vlog
 */
public class _1007ACeremonyinSanctum extends AbstractQuestHandler {

	public _1007ACeremonyinSanctum() {
		super(1007);
	}

	@Override
	public void register() {
		if (CustomConfig.ENABLE_SIMPLE_2NDCLASS)
			return;
		int[] npcs = { 790001, 203725, 203752, 203758, 203759, 203760, 203761, 801212, 801213 };
		qe.registerOnLevelChanged(questId);
		qe.registerOnQuestCompleted(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVars().getQuestVars();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 790001: // Pernos
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							if (var == 1) {
								return sendQuestDialog(env, 1013);
							}
							return false;
						case SETPRO1:
							if (var <= 1) {
								qs.setQuestVar(1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
								TeleportService.teleportTo(player, 110010000, 1313f, 1512f, 568f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
								return true;
							}
					}
					break;
				case 203725: // Leah
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						case SELECT2_1:
							return playQuestMovie(env, 92);
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2); // 2
					}
					break;
				case 203752: // Jucleas
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
							return false;
						case SELECT3_1:
							return playQuestMovie(env, 91);
						case SETPRO3:
							if (var == 2) {
								switch (player.getPlayerClass().getStartingClass()) {
									case WARRIOR:
										qs.setQuestVar(10);
										qs.setRewardGroup(0);
										break;
									case SCOUT:
										qs.setQuestVar(20);
										qs.setRewardGroup(1);
										break;
									case MAGE:
										qs.setQuestVar(30);
										qs.setRewardGroup(2);
										break;
									case PRIEST:
										qs.setQuestVar(40);
										qs.setRewardGroup(3);
										break;
									case ENGINEER:
										qs.setQuestVar(50);
										qs.setRewardGroup(4);
										break;
									case ARTIST:
										qs.setQuestVar(60);
										qs.setRewardGroup(5);
										break;
								}
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestSelectionDialog(env);
							}
							break;
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203758 && var == 10) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 2034);
					default:
						return sendQuestEndDialog(env);
				}
			} else if (targetId == 203759 && var == 20) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 2375);
					default:
						return sendQuestEndDialog(env);
				}
			} else if (targetId == 203760 && var == 30) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 2716);
					default:
						return sendQuestEndDialog(env);
				}
			} else if (targetId == 203761 && var == 40) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 3057);
					default:
						return sendQuestEndDialog(env);
				}
			} else if (targetId == 801212 && var == 50) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 3398);
					default:
						return sendQuestEndDialog(env);
				}
			} else if (targetId == 801213 && var == 60) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 3739);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 1006);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 1006);
	}
}
