package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Artur, Majka
 */
public class _14045RumorsOnWings extends AbstractQuestHandler {

	public _14045RumorsOnWings() {
		super(14045);
	}

	@Override
	public void register() {
		int[] npcs = { 278506, 279023, 278643 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 278506: // Tellus
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						case SELECT1_1_1:
							playQuestMovie(env, 272);
							break;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
					}
					break;
				case 279023: // Agemonerk
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						case SETPRO2:
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
							player.setState(CreatureState.FLYING);
							player.unsetState(CreatureState.ACTIVE);
							player.setFlightTeleportId(57001);
							PacketSendUtility.broadcastPacketAndReceive(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 57001, 0));
							return defaultCloseDialog(env, 1, 2); // 2
					}
					break;
				case 278643: // Raithor
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							} else if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
							return false;
						case SETPRO3:
							if (var == 2) {
								Npc raithor = (Npc) env.getVisibleObject();
								Npc attacker1 = (Npc) spawnForFiveMinutes(214102, raithor.getWorldMapInstance(), 2344.32f, 1789.96f, 2258.88f, (byte) 86);
								Npc attacker2 = (Npc) spawnForFiveMinutes(214102, raithor.getWorldMapInstance(), 2344.51f, 1786.01f, 2258.88f, (byte) 52);
								attacker1.getAggroList().addHate(raithor, 1);
								raithor.getAggroList().addHate(attacker1, 1);
								attacker2.getAggroList().addHate(raithor, 1);
								raithor.getAggroList().addHate(attacker2, 1);
								qs.setQuestVarById(0, 3); // 3
								return closeDialogWindow(env);
							}
							return false;
						case SETPRO4:
							if (var == 3) {
								qs.setQuestVarById(0, 12);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
								player.setState(CreatureState.FLYING);
								player.unsetState(CreatureState.ACTIVE);
								player.setFlightTeleportId(58001);
								PacketSendUtility.broadcastPacketAndReceive(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 58001, 0));
								return sendQuestSelectionDialog(env);
							}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 279023) { // Agemonerk
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		int[] quests = { 14040, 14041 };
		defaultOnQuestCompletedEvent(env, quests);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		int[] quests = { 14040, 14041 };
		defaultOnLevelChangedEvent(player, quests);
	}

}
