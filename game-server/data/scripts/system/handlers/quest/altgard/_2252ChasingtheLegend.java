package quest.altgard;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_USE_OBJECT;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 */
public class _2252ChasingtheLegend extends QuestHandler {

	private final static int questId = 2252;
	private final static int[] mob_ids = { 210634 }; // Minusha's Spirit

	public _2252ChasingtheLegend() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203646).addOnQuestStart(questId); // Sinood
		qe.registerQuestNpc(203646).addOnTalkEvent(questId);
		qe.registerQuestNpc(700060).addOnTalkEvent(questId); // Bone of Minusha
		for (int mob_id : mob_ids)
			qe.registerQuestNpc(mob_id).addOnKillEvent(questId);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		switch (targetId) {
			case 210634: // Minusha's Spirit
				if (var == 0) {
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(env);
					return true;
				}
		}
		return false;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 203646) {// Sinood
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 203646) {
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 1) {
							qs.setQuestVar(2);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestDialog(env, 1352);
						}
					case SELECT_QUEST_REWARD:
						if (var == 2)
							return sendQuestEndDialog(env);
				}
			}
			if (targetId == 700060) {
				switch (dialog) {
					case USE_OBJECT:
						if (var == 0) {
							PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.START_QUESTLOOT, 0, targetId), true);
							PacketSendUtility.sendPacket(player, new SM_USE_OBJECT(player.getObjectId(), targetId, 3000, 0));
							PacketSendUtility.broadcastPacket(player, new SM_EMOTION(player, EmotionType.END_QUESTLOOT, 0, targetId), true);
							Npc npc = (Npc) player.getTarget();
							if (npc == null || npc.getNpcId() != targetId)
								return false;
							QuestService.addNewSpawn(player.getWorldId(), player.getInstanceId(), 210634, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading()); // Minusha's Spirit
              ((Npc)player.getTarget()).getController().die();
						}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD)
			return sendQuestEndDialog(env);
		return false;
	}
}
