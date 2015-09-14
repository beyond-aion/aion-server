package quest.greater_stigma;

import java.util.Collection;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author kecimis
 */
public class _3932StopTheShulacks extends QuestHandler {

	private final static int questId = 3932;
	private final static int[] npc_ids = { 203711, 204656 };

	/**
	 * 203711 - Miriya 204656 - Maloren
	 */

	public _3932StopTheShulacks() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestSkill(18130, questId);
		qe.registerQuestNpc(203711).addOnQuestStart(questId);// Miriya
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onUseSkillEvent(QuestEnv env, int skillUsedId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			Collection<Npc> allNpcs = World.getInstance().getNpcs();
			for (Npc npc : allNpcs) {
				if (npc.getNpcId() == 700516) {
					npc.getController().die();
					npc.getController().onDelete();
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 203711)// Miriya
			{
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);

			}
			return false;
		}

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203711)// Miriya
			{
				return sendQuestEndDialog(env);
			}
			return false;
		} else if (qs.getStatus() == QuestStatus.START) {

			if (targetId == 203711 && var == 1)// Miriya
			{
				switch (env.getDialog()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					case CHECK_USER_HAS_QUEST_ITEM:
						if (QuestService.collectItemCheck(env, true)) {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestDialog(env, 5);
						} else
							return sendQuestDialog(env, 2716);
				}

			} else if (targetId == 204656 && var == 0)// Maloren
			{
				switch (env.getDialog()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1352);
					case SETPRO1:
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						return true;
				}
			}

			return false;
		}
		return false;
	}
}
