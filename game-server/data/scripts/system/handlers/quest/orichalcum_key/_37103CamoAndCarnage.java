package quest.orichalcum_key;

import static com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE.STR_MSG_DailyQuest_Ask_Mentee;

import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Cheatkiller
 */
public class _37103CamoAndCarnage extends QuestHandler {

	private final static int questId = 37103;

	public _37103CamoAndCarnage() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(700968).addOnTalkEvent(questId);
		qe.registerQuestNpc(799906).addOnTalkEvent(questId);
		qe.registerQuestNpc(217169).addOnKillEvent(questId);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 217169, 0, 5);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs == null || qs.isStartable()) {
			if (targetId == 0) {
				if (dialog == DialogAction.QUEST_ACCEPT_1) {
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				}
			}
		}
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 700968) {
				if (player.isInGroup2()) {
					PlayerGroup group = player.getPlayerGroup2();
					for (Player member : group.getMembers()) {
						if (member.isMentor() && MathUtil.getDistance(player, member) < GroupConfig.GROUP_MAX_DISTANCE) {
							Npc npc = (Npc) env.getVisibleObject();
							NpcActions.delete(npc, true);
							QuestService.addNewSpawn(npc.getWorldId(), npc.getInstanceId(), 217169, npc.getX(), npc.getY(), npc.getZ(), (byte) 0);
							return true;
						} else
							PacketSendUtility.sendPacket(player, STR_MSG_DailyQuest_Ask_Mentee());
					}
				}
			}
			if (targetId == 799906) {
				if (dialog == DialogAction.QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 5) {
						return sendQuestDialog(env, 1352);
					}
				} else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
					return defaultCloseDialog(env, 5, 5, true, true);
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799906) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 5);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
