package ai.worlds.kaldor;

import java.util.concurrent.TimeUnit;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

import ai.AggressiveNpcAI;

/**
 * Last modified: March 24th, 2018
 * 
 * @author Ritsu, Estrayl
 */
@AIName("berserk_anoha")
public class BerserkAnohaAI extends AggressiveNpcAI {

	public BerserkAnohaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		scheduleDespawn();
	}

	private void scheduleDespawn() {
		getOwner().getController().addTask(TaskId.DESPAWN, ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead()) {
				getOwner().getController().delete();
				broadcastAnnounce(SM_SYSTEM_MESSAGE.STR_MSG_ANOHA_DESPAWN());
			}
		}, 60 * 60000)); // 1hour
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
		Npc ca = (Npc) spawn(occupier == SiegeRace.ASMODIANS ? 804594 : 804595, 785.4833f, 458.4128f, 143.7177f, (byte) 30); // Commander Anoha
		ca.getController().addTask(TaskId.DESPAWN, ThreadPoolManager.getInstance().schedule(() -> ca.getController().delete(), 60, TimeUnit.MINUTES));
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
