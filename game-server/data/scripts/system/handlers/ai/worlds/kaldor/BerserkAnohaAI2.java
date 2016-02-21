package ai.worlds.kaldor;

import java.util.concurrent.Future;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author Ritsu
 * @modified Estrayl
 */
@AIName("berserk_anoha")
public class BerserkAnohaAI2 extends AggressiveNpcAI2 {

	private final String STR_MAIL_ANOHA = "To your Legion, for the successful vanquish of Berserk Anoha. Please take this reward in recognition of your service.\n- Commander Anoha";
	private Future<?> despawnTask;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		scheduleDespawn();
	}

	private void scheduleDespawn() {
		despawnTask = ThreadPoolManager.getInstance().schedule((Runnable) () -> {
			if (!isAlreadyDead()) {
				getOwner().getController().onDelete();
				broadcastAnnounce(SM_SYSTEM_MESSAGE.STR_MSG_ANOHA_DESPAWN());
			}
		}, 60 * 60000); // 1hour
	}

	@Override
	protected void handleDied() {
		if (despawnTask != null && !despawnTask.isCancelled())
			despawnTask.cancel(true);
		broadcastAnnounce(SM_SYSTEM_MESSAGE.STR_MSG_ANOHA_DIE());
		checkForFactionReward();
		getOwner().getKnownList().getKnownObjects().get(702618).getController().onDelete();
		super.handleDied();
	}

	private void checkForFactionReward() {
		SiegeRace occupier = SiegeService.getInstance().getFortress(7011).getRace();
		spawn(occupier == SiegeRace.ASMODIANS ? 804594 : 804595, 785.4833f, 458.4128f, 143.7177f, (byte) 30); // Commander Anoha
		// Legion Rewards
		int legionId = SiegeService.getInstance().getFortress(7011).getLegionId();
		int brigadeId = LegionService.getInstance().getLegionBGeneral(legionId);
		PlayerCommonData pcd = DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonData(brigadeId);
		// TODO: Refactore this!
		sendMail(pcd, 188053308, 20); // Illusion Godstone Bundle
		sendMail(pcd, 188053309, 100); // Ceramium Medal Bundle
		sendMail(pcd, 188053310, 20); // Eternal Ancient Manastone Bundle
		sendMail(pcd, 188053311, 25); // Fabled Ancient Manastone Bundle
		sendMail(pcd, 188053312, 30); // Heroic Ancient Manastone Bundle
		sendMail(pcd, 188053313, 35); // Superior Ancient Manastone Bundle
		sendMail(pcd, 188053314, 10); // L60 Composit Manastone Bundle
		sendMail(pcd, 188053315, 15); // L50 Composit Manastone Bundle
		sendMail(pcd, 188053316, 20); // L40 Composit Manastone Bundle
		sendMail(pcd, 188053317, 15); // Major Crown Bundle
		sendMail(pcd, 188053318, 20); // Greater Crown Bundle
		sendMail(pcd, 188053319, 25); // Crown Bundle
		sendMail(pcd, 188053320, 30); // Lesser Crown Bundle
	}

	private void sendMail(PlayerCommonData receiver, int itemId, int count) { // TODO: Find Retail Mail
		SystemMailService.getInstance().sendMail("Commander Anoha", receiver.getName(), "Victory Reward Notice", STR_MAIL_ANOHA, itemId, count, 0,
			LetterType.NORMAL);
	}

	private void broadcastAnnounce(SM_SYSTEM_MESSAGE msg) {
		World.getInstance().doOnAllPlayers(player -> PacketSendUtility.sendPacket(player, msg));
	}

	@Override
	protected AIAnswer pollInstance(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
				return AIAnswers.NEGATIVE;
			case SHOULD_RESPAWN:
				return AIAnswers.NEGATIVE;
			case SHOULD_REWARD:
				return AIAnswers.POSITIVE;
			case SHOULD_LOOT:
				return AIAnswers.NEGATIVE;
			default:
				return null;
		}
	}
}
