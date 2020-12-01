package quest.morheim;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Cheatkiller
 */
public class _2443TaisanMessage extends AbstractQuestHandler {

	public _2443TaisanMessage() {
		super(2443);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204403).addOnQuestStart(questId);
		qe.registerQuestNpc(790016).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (targetId == 204403) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204403:
					switch (env.getDialogActionId()) {
						case QUEST_SELECT: {
							if (qs.getQuestVarById(0) == 0)
								return sendQuestDialog(env, 1003);
							return false;
						}
						case SETPRO1:
						case QUEST_ACCEPT_1: {
							player.setState(CreatureState.FLYING);
							player.unsetState(CreatureState.ACTIVE);
							player.setFlightTeleportId(30001);
							PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 30001, 0));
							return defaultCloseDialog(env, 0, 1, true, false);
						}
						case SELECT1_1_1: {
							return sendQuestDialog(env, 1013);
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 790016) {
				if (env.getDialogActionId() == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
