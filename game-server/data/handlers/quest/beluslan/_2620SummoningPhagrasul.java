package quest.beluslan;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Nephis
 */
public class _2620SummoningPhagrasul extends AbstractQuestHandler {

	private final static int[] mob_ids = { 213109, 213111 };

	public _2620SummoningPhagrasul() {
		super(2620);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204787).addOnQuestStart(questId); // Chieftain Akagitan
		qe.registerQuestNpc(204787).addOnTalkEvent(questId);
		qe.registerQuestNpc(204824).addOnTalkEvent(questId); // Gigantic Phagrasul
		qe.registerQuestNpc(700323).addOnTalkEvent(questId); // Huge Mamut Skull
		for (int mob_id : mob_ids)
			qe.registerQuestNpc(mob_id).addOnKillEvent(questId);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		switch (targetId) {
			case 213109:
				if (qs.getQuestVarById(1) < 5 && qs.getQuestVarById(0) == 1) {
					qs.setQuestVarById(1, qs.getQuestVarById(1) + 1);
					updateQuestStatus(env);
					return true;
				}
				break;

			case 213111:
				if (qs.getQuestVarById(2) < 5 && qs.getQuestVarById(0) == 1) {
					qs.setQuestVarById(2, qs.getQuestVarById(2) + 1);
					updateQuestStatus(env);
					return true;
				}
		}

		return false;
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204787) {// Chieftain Akagitan
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else if (dialogActionId == QUEST_ACCEPT_1) {
					if (!giveQuestItem(env, 182204498, 1))
						return true;
					return sendQuestStartDialog(env);
				} else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 204824) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1011);
						return false;
					case SETPRO1:
						if (var == 0) {
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							final Npc npc = (Npc) env.getVisibleObject();
							ThreadPoolManager.getInstance().schedule(new Runnable() {

								@Override
								public void run() {
									npc.getController().delete();
								}
							}, 40000);
							return true;
						}
				}
			}
			if (targetId == 700323) {// Hugh mamut skull
				switch (dialogActionId) {
					case USE_OBJECT:
						if (var == 0) {
							final int targetObjectId = env.getVisibleObject().getObjectId();
							PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 1));
							PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.NEUTRALMODE_IN_STANDING, 0, targetObjectId), true);
							ThreadPoolManager.getInstance().schedule(new Runnable() {

								@Override
								public void run() {
									removeQuestItem(env, 182204498, 1);
									if (player.getTarget() == null || player.getTarget().getObjectId() != targetObjectId)
										return;
									PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetObjectId, 3000, 0));
									PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_LOOT, 0, targetObjectId), true);
									spawnForFiveMinutes(204824, player.getWorldMapInstance(), (float) 2851.698, (float) 160.88698, (float) 301.78537, (byte) 93);
								}
							}, 3000);
						}
				}
			}
			if (targetId == 204787) {// Chieftain Akagitan
				switch (dialogActionId) {
					case USE_OBJECT:
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestDialog(env, 10002);
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204787)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
