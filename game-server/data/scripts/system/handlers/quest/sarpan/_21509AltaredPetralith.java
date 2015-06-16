package quest.sarpan;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;


/**
 * @author zhkchi
 *
 */
public class _21509AltaredPetralith extends QuestHandler {

	private final static int questId = 21509;
	
	public _21509AltaredPetralith() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205782).addOnQuestStart(questId);
		qe.registerQuestNpc(205782).addOnTalkEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("WARRIORS_SHRINE_600020000"), questId);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = env.getTargetId();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205782)
			{
				switch (env.getDialog()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 4762);
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env);
					case QUEST_REFUSE_SIMPLE:
						return sendQuestEndDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205782)
				return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName == ZoneName.get("WARRIORS_SHRINE_600020000")) {
			final Player player = env.getPlayer();
			if (player == null)
				return false;
			final QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs == null)
				return false;

			if (qs.getQuestVars().getQuestVars() == 0) {
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}
}
