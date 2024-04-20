package quest.reshanta;

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
 * @author Artur, Majka
 */
public class _24045ASpeedyErrand extends AbstractQuestHandler {

	private final static int[] npc_ids = { 278034, 279004, 279024, 279006 };

	public _24045ASpeedyErrand() {
		super(24045);
	}

	@Override
	public void register() {
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 24040);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 24040);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 278034) {
				if (env.getDialogActionId() == USE_OBJECT)
					return sendQuestDialog(env, 10002);
				return sendQuestEndDialog(env);
			}
			return false;
		} else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 278034) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
					return false;
				case SETPRO1:
					return defaultCloseDialog(env, 0, 1);
			}
		} else if (targetId == 279004) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1352);
					return false;
				case SELECT2_1:
					playQuestMovie(env, 292);
					break;
				case SETPRO2:
					return defaultCloseDialog(env, 1, 2);
			}
		} else if (targetId == 279024) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 2)
						return sendQuestDialog(env, 1693);
					else if (var == 4)
						return sendQuestDialog(env, 2375);
					return false;
				case SETPRO3:
					if (defaultCloseDialog(env, 2, 3)) {
						player.setState(CreatureState.FLYING);
						player.unsetState(CreatureState.ACTIVE);
						player.setFlightTeleportId(55001);
						PacketSendUtility.broadcastPacketAndReceive(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 55001, 0));
						return true;
					}
					return false;
				case SETPRO5:
					return defaultCloseDialog(env, 4, 4, true, false);
			}
		} else if (targetId == 279006) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 3)
						return sendQuestDialog(env, 2034);
					return false;
				case SETPRO4:
					if (defaultCloseDialog(env, 3, 4)) {
						player.setState(CreatureState.FLYING);
						player.unsetState(CreatureState.ACTIVE);
						player.setFlightTeleportId(56001);
						PacketSendUtility.broadcastPacketAndReceive(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 56001, 0));
						return true;
					}
					return false;
			}
		}
		return false;
	}
}
