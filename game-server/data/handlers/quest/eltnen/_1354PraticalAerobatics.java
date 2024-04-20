package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Ritsu, Pad
 */
public class _1354PraticalAerobatics extends AbstractQuestHandler {

	// chronological order of the flight rings has been changed
	private String[] rings = { "ERACUS_TEMPLE_210020000_1", "ERACUS_TEMPLE_210020000_4", "ERACUS_TEMPLE_210020000_3", "ERACUS_TEMPLE_210020000_6",
		"ERACUS_TEMPLE_210020000_5", "ERACUS_TEMPLE_210020000_2", "ERACUS_TEMPLE_210020000_7" };

	public _1354PraticalAerobatics() {
		super(1354);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203983).addOnQuestStart(questId);
		qe.registerQuestNpc(203983).addOnTalkEvent(questId);
		qe.registerOnQuestTimerEnd(questId);
		for (String ring : rings) {
			qe.registerOnPassFlyingRings(ring, questId);
		}
	}

	@Override
	public boolean onPassFlyingRingEvent(QuestEnv env, String flyingRing) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (rings[0].equals(flyingRing))
				changeQuestStep(env, 1, 2);
			else if (rings[1].equals(flyingRing))
				changeQuestStep(env, 2, 3);
			else if (rings[2].equals(flyingRing))
				changeQuestStep(env, 3, 4);
			else if (rings[3].equals(flyingRing))
				changeQuestStep(env, 4, 5);
			else if (rings[4].equals(flyingRing))
				changeQuestStep(env, 5, 6);
			else if (rings[5].equals(flyingRing))
				changeQuestStep(env, 6, 7);
			else if (rings[6].equals(flyingRing)) {
				qs.setQuestVarById(0, 8);
				changeQuestStep(env, 8, 8, true);
				QuestService.questTimerEnd(env);
			}
			applyFlightRingEffect(env);
			return true;
		}
		return false;
	}

	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		qs.setQuestVarById(0, 0);
		updateQuestStatus(env);
		return true;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		int dialogActionId = env.getDialogActionId();
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (targetId == 203983) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203983) {
				int var0 = qs.getQuestVarById(0);
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var0 == 0)
							return sendQuestDialog(env, 1003);
						else if (var0 == 1)
							return sendQuestDialog(env, 2717);
						else if (var0 == 8)
							return sendQuestDialog(env, 2375);
						return false;
					case SETPRO1:
						if (qs.getQuestVarById(0) == 0) {
							QuestService.questTimerStart(env, 120);
							return defaultCloseDialog(env, 0, 1);
						}
						return false;
					case SELECT_QUEST_REWARD:
						return sendQuestEndDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203983)
				return sendQuestEndDialog(env);
		}
		return false;
	}

	private void applyFlightRingEffect(QuestEnv env) {
		Player player = env.getPlayer();
		player.getLifeStats().increaseFp(TYPE.FP_RINGS, 7, 0, LOG.REGULAR);
		SkillEngine.getInstance().applyEffectDirectly(1856, player, player);
	}

}
