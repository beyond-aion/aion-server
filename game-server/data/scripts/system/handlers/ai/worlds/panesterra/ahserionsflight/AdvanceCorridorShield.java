package ai.worlds.panesterra.ahserionsflight;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaid;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaidStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;


/**
 * @author Yeats
 *
 */
@AIName("ahserion_advance_corridor_shield")
public class AdvanceCorridorShield extends NpcAI {
		
	private AtomicBoolean canShout = new AtomicBoolean(true);
	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		broadcastAttack();
	}
	
	private void broadcastAttack() {
		if (canShout.compareAndSet(true, false)) {
			if (getOwner() != null && !getOwner().getLifeStats().isAlreadyDead()
				&& getOwner().getWorldId() == 400030000) {
				switch (getOwner().getNpcId()) {
					case 297306:
						sendPacket(1402266);
						break;
					case 297307:
						sendPacket(1402267);
						break;
					case 297308:
						sendPacket(1402268);
						break;
					case 297309:
						sendPacket(1402269);
						break;
				}
				allowShout();
			}
		}
	}
	
	private void allowShout() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				if (getOwner() != null && !getOwner().getLifeStats().isAlreadyDead()) {
					canShout.set(true);
				}
			}
		}, 3000);
	}
	
	private void sendPacket(int msgId) {
		World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().forEachPlayer(new Consumer<Player>() {
      @Override
      public void accept(Player player) {
      	PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(msgId));
      }
		});
	}
	
	@Override
	public void handleDied() {
		if (getOwner().getWorldId() == 400030000) {
			if (AhserionRaid.getInstance().getStatus() == AhserionRaidStatus.INSTANCE_RUNNING) {
				switch (getOwner().getNpcId()) {
					case 297306:
						sendPacket(1402270);
						break;
					case 297307:
						sendPacket(1402271);
						break;
					case 297308:
						sendPacket(1402272);
						break;
					case 297309:
						sendPacket(1402273);
							break;
				}
				AhserionRaid.getInstance().corridorShieldDestroyed(getOwner().getNpcId());
			}
		}
		super.handleDied();
	}
	
	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_RESPAWN:
			case SHOULD_LOOT:
				return false;
			default:
				return super.ask(question);
		}
	}
}
