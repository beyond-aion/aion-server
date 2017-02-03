package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Balthazar
 */

public class _1393NewFlightPath extends QuestHandler {

	public _1393NewFlightPath() {
		super(1393);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204041).addOnQuestStart(questId);
		qe.registerQuestNpc(204041).addOnTalkEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("LEPHARIST_BASTION_210020000"), questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204041) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					default:
						return sendQuestStartDialog(env);
				}
			}
		}
		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 204041) {
				switch (env.getDialogActionId()) {
					case SETPRO1:
						player.setState(CreatureState.FLYING);
						player.unsetState(CreatureState.ACTIVE);
						player.setFlightTeleportId(17001);
						PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FLYTELEPORT, 17001, 0));
						return defaultCloseDialog(env, 0, 1, true, false);
					default:
						return closeDialogWindow(env);
				}
			}
		}

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204041) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
