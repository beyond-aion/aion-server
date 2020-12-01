package quest.poeta;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author MrPoke
 */
public class _1000Prologue extends AbstractQuestHandler {

	public _1000Prologue() {
		super(1000);
	}

	@Override
	public void register() {
		qe.registerOnEnterWorld(questId);
		qe.registerOnMovieEndQuest(1, questId);
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		if (player.getRace() == Race.ELYOS && !player.getQuestStateList().hasQuest(questId)) {
			env.setQuestId(questId);
			if (QuestService.startQuest(env) || player.getQuestStateList().getQuestState(questId).getStatus() == QuestStatus.START) {
				PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(1, 1));
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId != 1)
			return false;
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;
		qs.setStatus(QuestStatus.REWARD);
		QuestService.finishQuest(env);
		return true;
	}
}
