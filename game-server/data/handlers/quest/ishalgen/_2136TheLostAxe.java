package quest.ishalgen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Rhys2002, Hellboy
 */
public class _2136TheLostAxe extends AbstractQuestHandler {

	private final static int[] npc_ids = { 700146, 790009 };

	public _2136TheLostAxe() {
		super(2136);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182203130, questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (env.getDialogActionId() == QUEST_ACCEPT_1) {
				QuestService.startQuest(env);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
				return true;
			} else
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(0, 0));
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 790009) {
				final Npc npc = (Npc) env.getVisibleObject();
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						npc.getController().delete();
					}
				}, 10000);
				return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() != QuestStatus.START)
			return false;

		if (targetId == 790009) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1011);
					return false;
				case SETPRO1:
					if (var == 1) {
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						removeQuestItem(env, 182203130, 1);
						qs.setRewardGroup(1);
						return sendQuestDialog(env, 6);
					}
					return false;
				case SETPRO2:
					if (var == 1) {
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						removeQuestItem(env, 182203130, 1);
						qs.setRewardGroup(0);
						return sendQuestDialog(env, 5);
					}
			}
		} else if (targetId == 700146) {
			switch (env.getDialogActionId()) {
				case USE_OBJECT:
					if (var == 0) {
						playQuestMovie(env, 59);
						qs.setQuestVarById(0, 1);
						updateQuestStatus(env);
						spawnForFiveMinutes(790009, player.getWorldMapInstance(), 1088.5f, 2371.8f, 258.375f, (byte) 87);
						return true;
					}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (id != 182203130)
			return HandlerResult.UNKNOWN;
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 20, 1, 0), true);
		if (qs == null || qs.isStartable()) {
			QuestService.startQuest(env);
		}
		return HandlerResult.SUCCESS;
	}
}
