package ai.worlds.kaldor;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.poll.AIAnswer;
import com.aionemu.gameserver.ai2.poll.AIAnswers;
import com.aionemu.gameserver.ai2.poll.AIQuestion;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Ritsu
 * @modified Estrayl
 */
@AIName("berserk_anoha")
public class BerserkAnohaAI2 extends AggressiveNpcAI2 {
	
	private Future<?> despawnTask;
	private AtomicBoolean isSpawned = new AtomicBoolean(false);
	
	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		scheduleDespawn();
		if (isSpawned.compareAndSet(false, true));
	}
	
	private void scheduleDespawn() {
		despawnTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (!isAlreadyDead()) {
					getOwner().getController().onDelete();
					broadcastAnnounce(1402505);
				}
			}
		}, 60 * 60000); // 1hour
	}
	
	@Override
	protected void handleDied() {
		cancelDespawnTask();
		broadcastAnnounce(1402504); // Died
		checkForFactionReward();
		getOwner().getKnownList().getKnownObjects().get(702618).getController().onDelete();
		super.handleDied();
	}
	
	private void checkForFactionReward() {
		spawnCommanderAnoha(getCommanderId());		
		//Legion Rewards
		int legionId = SiegeService.getInstance().getFortress(7011).getLegionId();
		int brigadeId = LegionService.getInstance().getLegionBGeneral(legionId);
		PlayerCommonData pcd = DAOManager.getDAO(PlayerDAO.class).loadPlayerCommonData(brigadeId);
		//TODO: Refactore this!
		sendMail(pcd, 188053308, 10); //Illusion Godstone Bundle				
		sendMail(pcd, 188053309, 75); //Ceramium Medal Bundle
		sendMail(pcd, 188053310, 10); //Eternal Ancient Manastone Bundle
		sendMail(pcd, 188053311, 15); //Fabled Ancient Manastone Bundle
		sendMail(pcd, 188053312, 20); //Heroic Ancient Manastone Bundle
		sendMail(pcd, 188053313, 25); //Superior Ancient Manastone Bundle
		sendMail(pcd, 188053314, 10); //L60 Composit Manastone Bundle
		sendMail(pcd, 188053315, 15); //L50 Composit Manastone Bundle
		sendMail(pcd, 188053316, 20); //L40 Composit Manastone Bundle
		sendMail(pcd, 188053317, 5);  //Major Crown Bundle
		sendMail(pcd, 188053318, 10); //Greater Crown Bundle
		sendMail(pcd, 188053319, 15); //Crown Bundle
		sendMail(pcd, 188053320, 20); //Lesser Crown Bundle
	}
	
	private void sendMail(PlayerCommonData receiver, int itemId, int count) { //TODO: Find Retail Mail
		if (receiver.getMailboxLetters() < 100)
			SystemMailService.getInstance().sendMail("Commander Anoha", receiver.getName(), "Victory Reward Notice", "To your Legion, for the successful "
				+ "vanquish of Berserk Anoha. Please take this reward in recognition of your service.\n- Commander Anoha" , itemId, count, 0, LetterType.NORMAL);
		else { //Players will cry a lot if they miss items because of an overflowed mailbox
			Player brigade = receiver.getPlayer();
			if (brigade != null) {
				if (!brigade.getInventory().isFull())
					ItemService.addItem(brigade, itemId, count);
				else //blame them for being stupid.
					PacketSendUtility.sendMessage(brigade, "Next time you should get enough place in your mailbox and cube before fighting such a worldboss!");
			}
		}
	}
	
	private void spawnCommanderAnoha(int npcId) {
		if (npcId != 0)
			spawn(npcId, 785.4833f, 458.4128f, 143.7177f, (byte) 30);
	}
	
	private int getCommanderId() {
		SiegeRace occupier = SiegeService.getInstance().getFortress(7011).getRace();
		switch (occupier) {
			case ASMODIANS:
				return 804594;
			case ELYOS:
				return 804595;
			default:
				return 0;
		}
	}
	
	private void cancelDespawnTask() {
		if (despawnTask != null && !despawnTask.isCancelled())
			despawnTask.cancel(true);
	}
	
	private void broadcastAnnounce(final int msgId) {
		World.getInstance().doOnAllPlayers(new Visitor<Player>() {
			@Override
			public void visit(Player player) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(msgId));
			}
		});
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
