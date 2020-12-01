package quest.levinshor;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Pad
 */
public class _13744AgentinNeed extends AbstractQuestHandler {

	public _13744AgentinNeed() {
		super(13744);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(832840).addOnTalkEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("DRAGON_LORDS_SHRINE_600100000"), questId);
		qe.registerOnEnterZone(ZoneName.get("FLAMEBERTH_DOWNS_600100000"), questId);
		qe.registerOnKillInZone("DRAGON_LORDS_SHRINE_600100000", questId);
		qe.registerOnKillInZone("FLAMEBERTH_DOWNS_600100000", questId);
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if ((zoneName == ZoneName.get("DRAGON_LORDS_SHRINE_600100000") || zoneName == ZoneName.get("FLAMEBERTH_DOWNS_600100000"))
			&& SiegeService.getInstance().isSiegeInProgress(8011)) {
			Player player = env.getPlayer();
			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs == null || qs.isStartable()) {
				return QuestService.startQuest(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillInZoneEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		VisibleObject target = env.getVisibleObject();
		if (target instanceof Player
			&& (player.isInsideZone(ZoneName.get("DRAGON_LORDS_SHRINE_600100000")) || player.isInsideZone(ZoneName.get("FLAMEBERTH_DOWNS_600100000")))
			&& SiegeService.getInstance().isSiegeInProgress(8011)) {
			if ((player.getLevel() >= (((Player) target).getLevel() - 5)) && (player.getLevel() <= (((Player) target).getLevel() + 9))) {
				int var1 = qs.getQuestVarById(1);
				if (var1 >= 0 && var1 < 11) {
					qs.setQuestVarById(1, var1 + 1);
					updateQuestStatus(env);
					return true;
				} else if (var1 == 11) {
					qs.setQuestVarById(0, 1);
					qs.setQuestVarById(1, 0);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (env.getTargetId() == 832840) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 10002);
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
