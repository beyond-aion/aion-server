package quest.rentus_base;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Ritsu
 */
public class _30552DriveOutTheVasharti extends QuestHandler {

	private final static int questId = 30552;
	private final static int[] mobIds = { 217307, 217308, 217313 };

	public _30552DriveOutTheVasharti() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799666).addOnQuestStart(questId); // ariana
		qe.registerQuestNpc(799666).addOnTalkEvent(questId);
		qe.registerQuestNpc(799670).addOnQuestStart(questId); // ariana
		qe.registerOnEnterZone(ZoneName.get("SPARRING_GROUNDS_300280000"), questId);
		qe.registerOnEnterZone(ZoneName.get("SIELS_FORGE_300280000"), questId);
		for (int id : mobIds)
			qe.registerQuestNpc(id).addOnKillEvent(questId);
		qe.registerQuestNpc(799544).addOnTalkEvent(questId); // Oreitia
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 799666) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 4762);
					default:
						return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 799670:
					switch (dialog) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 2716);
						case SET_SUCCEED:
							return defaultCloseDialog(env, 5, 6, false, false);
					}
					break;
				case 799544:
					switch (dialog) {
						case USE_OBJECT:
							return sendQuestDialog(env, 10002);
						case SELECT_QUEST_REWARD:
							return defaultCloseDialog(env, 6, 6, true, true);
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch (targetId) {
				case 799544:
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (zoneName.equals(ZoneName.get("SPARRING_GROUNDS_300280000"))) {
				if (var == 0) {
					changeQuestStep(env, 0, 1, false); // 1
					return true;
				}

			} else if (zoneName.equals(ZoneName.get("SIELS_FORGE_300280000"))) {
				if (var == 2) {
					qs.setQuestVar(3);
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;
		int var = qs.getQuestVarById(1);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		switch (targetId) {
			case 217307:
				if (qs.getQuestVarById(0) == 1) {
					qs.setQuestVarById(1, var + 1);
					updateQuestStatus(env);
					return true;
				}
			case 217308: {
				qs.setQuestVarById(0, 2);
				updateQuestStatus(env);
				return true;
			}

			case 217313:
				if (qs.getQuestVarById(0) == 3) {
					qs.setQuestVar(5);
					updateQuestStatus(env);
					return true;
				}
		}
		return false;
	}
}
