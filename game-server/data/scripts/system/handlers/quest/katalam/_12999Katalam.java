package quest.katalam;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;


/**
 * @author Cheatkiller
 *
 */
public class _12999Katalam extends QuestHandler {

	private final static int questId = 12999;

	public _12999Katalam() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterWorld(questId);
		qe.registerOnMovieEndQuest(26, questId);
	}
	
	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			if (player.getWorldId() == 600050000) {
				env.setQuestId(questId);
				QuestService.startQuest(env);
				PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(1, 26));
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			qs.setStatus(QuestStatus.REWARD);
			QuestService.finishQuest(env);
			return true;
		}
		return false;
	}
}
