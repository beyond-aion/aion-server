package quest.altgard;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Artur, Majka
 */
public class _24015TotemPlowed extends AbstractQuestHandler {

	public _24015TotemPlowed() {
		super(24015);
	}

	@Override
	public void register() {
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(203669).addOnTalkEvent(questId); // Taora
		qe.registerQuestNpc(203557).addOnTalkEvent(questId); // Suthran
		qe.registerQuestNpc(700099).addOnKillEvent(questId); // Zemurru's Totem
		qe.registerOnEnterZone(ZoneName.get("DF1A_SENSORYAREA_Q2021_206013_2_220030000"), questId);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		if (var >= 2 && var < 4) {
			qs.setQuestVarById(0, var + 1);
			updateQuestStatus(env);
			((Npc) env.getVisibleObject()).getController().delete();
			return true;
		} else if (var == 4) {
			qs.setStatus(QuestStatus.REWARD);
			updateQuestStatus(env);
			((Npc) env.getVisibleObject()).getController().delete();
			return true;
		}
		return false;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203669: // Taora
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1011);
						case SELECT1_1_1:
							playQuestMovie(env, 218);
							qs.setQuestVarById(0, 1);
							updateQuestStatus(env);
							return sendQuestDialog(env, 1013);
						case SETPRO1:
							return closeDialogWindow(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203557) { // Suthran
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 1352);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName != ZoneName.get("DF1A_SENSORYAREA_Q2021_206013_2_220030000"))
			return false;
		final Player player = env.getPlayer();
		if (player == null)
			return false;
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		if (qs.getQuestVarById(0) == 1) {
			qs.setQuestVarById(0, 2);
			updateQuestStatus(env);
			return true;
		}
		return false;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 24010);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 24010);
	}

}
