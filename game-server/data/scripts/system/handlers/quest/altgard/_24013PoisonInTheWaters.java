package quest.altgard;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Artur
 * @rework Ritsu
 */
public class _24013PoisonInTheWaters extends QuestHandler {

	private final static int questId = 24013;
	private final static int[] mobs = { 210455, 210456, 214039, 210458, 214032 };
	
	public _24013PoisonInTheWaters() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		qe.registerQuestItem(182215359, questId);
		qe.registerQuestNpc(203631).addOnTalkEvent(questId);
		qe.registerQuestNpc(203621).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		final int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203631:
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							break;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
					}
				case 203621:
					switch (env.getDialog()){
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
							break;
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2, 182215359,1); // 2
					}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203631) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		if (player.isInsideZone(ZoneName.get("DF1A_ITEMUSEAREA_Q2016"))) {
			return HandlerResult.fromBoolean(useQuestItem(env, item, 2, 3, false)); // 3
		}
		return HandlerResult.FAILED;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env) {
	   if (defaultOnKillEvent(env, mobs, 7, true))
		  return true;
		return defaultOnKillEvent(env, mobs, 3, 7); // 6
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env, 24012);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 24010, true);
	}
}
