package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;


/**
 * @author mr.madison
 *
 */
public class _41513ItsaMinefieldOutThere extends QuestHandler {

	private final static int questId = 41513;

	public _41513ItsaMinefieldOutThere() {
		super(questId);
	}
	
	@Override
	public void register() {
		qe.registerQuestNpc(205947).addOnQuestStart(questId);
		qe.registerQuestNpc(205947).addOnTalkEvent(questId);
		qe.registerQuestItem(182212520, questId);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env){
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205947) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 4762);
					}
					case QUEST_ACCEPT_SIMPLE: {
						return sendQuestStartDialog(env, 182212520, 1);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205947) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
	
	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.isInsideZone(ZoneName.get("LDF4B_ITEMUSEAREA_Q41513A")) && item.getItemId() == 182212520) {
				changeQuestStep(env, 0, 0, true);
			}
		}
		return HandlerResult.FAILED;
	}
	
}
