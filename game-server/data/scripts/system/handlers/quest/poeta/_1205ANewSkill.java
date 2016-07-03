package quest.poeta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author MrPoke
 */
public class _1205ANewSkill extends QuestHandler {

	public _1205ANewSkill() {
		super(1205);
	}

	@Override
	public void register() {
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(203087).addOnTalkEvent(questId); // Warrior
		qe.registerQuestNpc(203088).addOnTalkEvent(questId); // Scout
		qe.registerQuestNpc(203089).addOnTalkEvent(questId); // Mage
		qe.registerQuestNpc(203090).addOnTalkEvent(questId); // Priest
		qe.registerQuestNpc(801210).addOnTalkEvent(questId); // Engineer
		qe.registerQuestNpc(801211).addOnTalkEvent(questId); // Artist
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		if (player.getQuestStateList().hasQuest(questId))
			return;
		QuestEnv env = new QuestEnv(null, player, questId, 0);
		if (QuestService.startQuest(env)) {
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			qs.setStatus(QuestStatus.REWARD);
			PlayerClass playerClass = player.getPlayerClass();
			if (!playerClass.isStartingClass())
				playerClass = PlayerClass.getStartingClassFor(playerClass);
			switch (playerClass) {
				case WARRIOR:
					qs.setQuestVar(1);
					break;
				case SCOUT:
					qs.setQuestVar(2);
					break;
				case MAGE:
					qs.setQuestVar(3);
					break;
				case PRIEST:
					qs.setQuestVar(4);
					break;
				case ENGINEER:
					qs.setQuestVar(5);
					break;
				case ARTIST:
					qs.setQuestVar(6);
					break;
			}
			updateQuestStatus(env);
		}
		return;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.REWARD)
			return false;

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		PlayerClass playerClass = PlayerClass.getStartingClassFor(player.getCommonData().getPlayerClass());
		switch (targetId) {
			case 203087:
				if (playerClass == PlayerClass.WARRIOR) {
					if (env.getDialog() == DialogAction.USE_OBJECT)
						return sendQuestDialog(env, 1011);
					else
						return sendQuestEndDialog(env, 0);
				}
				return false;
			case 203088:
				if (playerClass == PlayerClass.SCOUT) {
					if (env.getDialog() == DialogAction.USE_OBJECT)
						return sendQuestDialog(env, 1352);
					else
						return sendQuestEndDialog(env, 1);
				}
				return false;
			case 203089:
				if (playerClass == PlayerClass.MAGE) {
					if (env.getDialog() == DialogAction.USE_OBJECT)
						return sendQuestDialog(env, 1693);
					else
						return sendQuestEndDialog(env, 2);
				}
				return false;
			case 203090:
				if (playerClass == PlayerClass.PRIEST) {
					if (env.getDialog() == DialogAction.USE_OBJECT)
						return sendQuestDialog(env, 2034);
					else
						return sendQuestEndDialog(env, 3);
				}
				return false;
			case 801210:
				if (playerClass == PlayerClass.ENGINEER) {
					if (env.getDialog() == DialogAction.USE_OBJECT)
						return sendQuestDialog(env, 2375);
					else
						return sendQuestEndDialog(env, 4);
				}
				return false;
			case 801211:
				if (playerClass == PlayerClass.ARTIST) {
					if (env.getDialog() == DialogAction.USE_OBJECT)
						return sendQuestDialog(env, 2716);
					else
						return sendQuestEndDialog(env, 5);
				}
				return false;
		}

		return false;
	}
}
