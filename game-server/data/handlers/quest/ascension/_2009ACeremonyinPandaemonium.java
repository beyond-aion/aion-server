package quest.ascension;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.configs.main.CustomConfig;
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

/**
 * @author MrPoke
 */
public class _2009ACeremonyinPandaemonium extends AbstractQuestHandler {

	public _2009ACeremonyinPandaemonium() {
		super(2009);
	}

	@Override
	public void register() {
		if (CustomConfig.ENABLE_SIMPLE_2NDCLASS)
			return;
		qe.registerOnLevelChanged(questId);
		qe.registerOnQuestCompleted(questId);
		qe.registerQuestNpc(203550).addOnTalkEvent(questId);
		qe.registerQuestNpc(204182).addOnTalkEvent(questId);
		qe.registerQuestNpc(204075).addOnTalkEvent(questId);
		qe.registerQuestNpc(204080).addOnTalkEvent(questId);
		qe.registerQuestNpc(204081).addOnTalkEvent(questId);
		qe.registerQuestNpc(204082).addOnTalkEvent(questId);
		qe.registerQuestNpc(204083).addOnTalkEvent(questId);
		qe.registerQuestNpc(801220).addOnTalkEvent(questId);
		qe.registerQuestNpc(801221).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVars().getQuestVars();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203550) {
				switch (env.getDialogActionId()) {
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
							TeleportService.teleportTo(player, 120010000, 1685f, 1400f, 195f, (byte) 0, TeleportAnimation.FADE_OUT_BEAM);
							return true;
						}
				}
			} else if (targetId == 204182) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 1)
							return sendQuestDialog(env, 1352);
						return false;
					case SELECT2_1:
						if (var == 1) {
							playQuestMovie(env, 121);
							return false;
						}
						return false;
					case SETPRO2:
						return defaultCloseDialog(env, 1, 2); // 2
				}
			} else if (targetId == 204075) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 2)
							return sendQuestDialog(env, 1693);
						return false;
					case SELECT3_1:
						if (var == 2) {
							playQuestMovie(env, 122);
							return false;
						}
						return false;
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
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204080 && var == 10) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 2034);
					default:
						return sendQuestEndDialog(env);
				}
			} else if (targetId == 204081 && var == 20) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 2375);
					default:
						return sendQuestEndDialog(env);
				}
			} else if (targetId == 204082 && var == 30) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 2716);
					default:
						return sendQuestEndDialog(env);
				}
			} else if (targetId == 204083 && var == 40) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 3057);
					default:
						return sendQuestEndDialog(env);
				}
			} else if (targetId == 801220 && var == 50) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 3398);
					default:
						return sendQuestEndDialog(env);
				}
			} else if (targetId == 801221 && var == 60) {
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
		defaultOnLevelChangedEvent(player, 2008);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 2008);
	}
}
