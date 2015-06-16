package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.zone.ZoneName;


/**
 * @author Cheatkiller
 *
 */
public class _41527ANiceHealingBath extends QuestHandler {

	private final static int questId = 41527;

	public _41527ANiceHealingBath() {
		super(questId);
	}

	public void register() {
		qe.registerQuestNpc(205895).addOnQuestStart(questId);
		qe.registerQuestNpc(205964).addOnTalkEvent(questId);
		qe.registerQuestNpc(205964).addOnTalkEvent(questId);
		qe.registerQuestNpc(219206).addOnKillEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("STEAM_SPRINGS_600030000"), questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205895) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
		if (targetId == 205964) {
			switch (dialog) {
				case QUEST_SELECT: {
					return sendQuestDialog(env, 1011);
				}
				case SETPRO1: {
					return defaultCloseDialog(env, 0, 1);
				}
			}
		}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205964) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 10002);
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
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 1) {
				SkillEngine.getInstance().applyEffectDirectly(20437, player, player, (60 * 1000));
				return defaultOnKillEvent(env, 219206, 1, 2);
			}
		}
		return false;
	}
	
	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName == ZoneName.get("STEAM_SPRINGS_600030000")) {
			Player player = env.getPlayer();
			if (player == null)
				return false;
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);
				if (var == 2) {
					SkillEngine.getInstance().applyEffectDirectly(8756, player, player, 0);
					changeQuestStep(env, 2, 2, true);
					return true;
				}
			}
		}
		return false;
	}
}

