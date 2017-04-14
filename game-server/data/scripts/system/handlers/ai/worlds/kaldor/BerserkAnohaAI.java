package ai.worlds.kaldor;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 * @modified Estrayl
 */
@AIName("berserk_anoha")
public class BerserkAnohaAI extends AggressiveNpcAI {

	private final String STR_MAIL_ANOHA = "To your Legion, for the successful vanquish of Berserk Anoha. Please take this reward in recognition of your service.\n- Commander Anoha";

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		scheduleDespawn();
	}

	private void scheduleDespawn() {
		getOwner().getController().addTask(TaskId.DESPAWN, ThreadPoolManager.getInstance().schedule(() -> {
			if (!isAlreadyDead()) {
				getOwner().getController().delete();
				broadcastAnnounce(SM_SYSTEM_MESSAGE.STR_MSG_ANOHA_DESPAWN());
			}
		} , 60 * 60000)); // 1hour
	}

	@Override
	protected void handleDespawned() {
		Npc flag = getOwner().getPosition().getWorldMapInstance().getNpc(702618); // see AnohasSword AI
		if (flag != null)
			flag.getController().delete();
		super.handleDespawned();
	};

	@Override
	protected void handleDied() {
		getOwner().getController().cancelTask(TaskId.DESPAWN);
		broadcastAnnounce(SM_SYSTEM_MESSAGE.STR_MSG_ANOHA_DIE());
		checkForFactionReward();
		super.handleDied();
	}

	private void checkForFactionReward() {
		SiegeRace occupier = SiegeService.getInstance().getFortress(7011).getRace();
		spawn(occupier == SiegeRace.ASMODIANS ? 804594 : 804595, 785.4833f, 458.4128f, 143.7177f, (byte) 30); // Commander Anoha
		// Legion Rewards
		int legionId = SiegeService.getInstance().getFortress(7011).getLegionId();
		int brigadeId = LegionService.getInstance().getBrigadeGeneralOfLegion(legionId);
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
		World.getInstance().forEachPlayer(player -> PacketSendUtility.sendPacket(player, msg));
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_LOOT:
				return false;
		}
		return super.ask(question);
	}
}
