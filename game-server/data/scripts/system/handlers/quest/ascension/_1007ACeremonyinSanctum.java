package quest.ascension;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke + Dune11
 * @reworked vlog
 */
public class _1007ACeremonyinSanctum extends QuestHandler {

	private final static int questId = 1007;

	public _1007ACeremonyinSanctum() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 790001, 203725, 203752, 203758, 203759, 203760, 203761, 801212, 801213 };
		if (CustomConfig.ENABLE_SIMPLE_2NDCLASS) {
			return;
		}
		qe.registerOnLevelUp(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVars().getQuestVars();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 790001: { // Pernos
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							if (var == 1) {
								return sendQuestDialog(env, 1013);
							}
						case SETPRO1:
							if (var <= 1) {
								qs.setQuestVar(1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
								TeleportService2.teleportTo(player, 110010000, 1313f, 1512f, 568f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
								return true;
							}
					}
					break;
				}
				case 203725: { // Leah
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
						}
						case SELECT_ACTION_1353: {
							return playQuestMovie(env, 92);
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2); // 2
						}
					}
					break;
				}
				case 203752: { // Jucleas
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
						}
						case SELECT_ACTION_1694: {
							return playQuestMovie(env, 91);
						}
						case SETPRO3: {
							if (var == 2) {
								PlayerClass playerClass = PlayerClass.getStartingClassFor(player.getCommonData().getPlayerClass());
								switch (playerClass) {
									case WARRIOR: {
										qs.setQuestVar(10);
										break;
									}
									case SCOUT: {
										qs.setQuestVar(20);
										break;
									}
									case MAGE: {
										qs.setQuestVar(30);
										break;
									}
									case PRIEST: {
										qs.setQuestVar(40);
										break;
									}
									case ENGINEER: {
										qs.setQuestVar(50);
										break;
									}
									case ARTIST: {
										qs.setQuestVar(60);
										break;
									}
								}
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestSelectionDialog(env);
							}
						}
							break;
					}
				}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203758 && var == 10) {
				switch (env.getDialogId()) {
					case -1:
						return sendQuestDialog(env, 2034);
					default:
						return sendQuestEndDialog(env, 0);
				}
			} else if (targetId == 203759 && var == 20) {
				switch (env.getDialogId()) {
					case -1:
						return sendQuestDialog(env, 2375);
					default:
						return sendQuestEndDialog(env, 1);
				}
			} else if (targetId == 203760 && var == 30) {
				switch (env.getDialogId()) {
					case -1:
						return sendQuestDialog(env, 2716);
					default:
						return sendQuestEndDialog(env, 2);
				}
			} else if (targetId == 203761 && var == 40) {
				switch (env.getDialogId()) {
					case -1:
						return sendQuestDialog(env, 3057);
					default:
						return sendQuestEndDialog(env, 3);
				}
			} else if (targetId == 801212 && var == 50) {
				switch (env.getDialogId()) {
					case -1:
						return sendQuestDialog(env, 3398);
					default:
						return sendQuestEndDialog(env, 4);
				}
			} else if (targetId == 801213 && var == 60) {
				switch (env.getDialogId()) {
					case -1:
						return sendQuestDialog(env, 3739);
					default:
						return sendQuestEndDialog(env, 5);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 1006);
	}
}
