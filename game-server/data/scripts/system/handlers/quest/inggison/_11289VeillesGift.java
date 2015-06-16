package quest.inggison;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;


/**
 * @author zhkchi
 *
 */
public class _11289VeillesGift extends QuestHandler {

	private final static int questId = 11289;

	public _11289VeillesGift() {
		super(questId);
	}
	
	@Override
	public void register() {
		qe.registerQuestNpc(799038).addOnTalkEvent(questId);
		qe.registerQuestItem(182213147, questId);
	}
	
	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (dialog == DialogAction.QUEST_SELECT) {
				return sendQuestDialog(env, 4);
			}else{
				return sendQuestStartDialog(env);
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.START) {
			if(targetId == 799038){
				if(dialog == DialogAction.QUEST_SELECT){
					if(checkItemExistence(env, 182213147, 1, true)){
						changeQuestStep(env, 0, 0, true);
						return sendQuestDialog(env, 2375);
					}
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799038) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 2375);
					}
					case SELECT_QUEST_REWARD: {
						return sendQuestDialog(env, 5);
					}
					default: {
						return sendQuestEndDialog(env);
					}
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

		if (id != 182213147)
			return HandlerResult.UNKNOWN;
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 20, 1, 0), true);
		if (qs == null || qs.getStatus() == QuestStatus.NONE)
			sendQuestDialog(env, 4);
		return HandlerResult.SUCCESS;
	}
}
