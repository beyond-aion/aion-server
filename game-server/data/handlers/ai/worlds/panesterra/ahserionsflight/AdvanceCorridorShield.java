package ai.worlds.panesterra.ahserionsflight;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.services.panesterra.ahserion.AhserionRaid;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author Yeats
 */
@AIName("ahserion_advance_corridor_shield")
public class AdvanceCorridorShield extends NpcAI {

	private AtomicBoolean canShout = new AtomicBoolean(true);

	public AdvanceCorridorShield(Npc owner) {
		super(owner);
	}

	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		broadcastAttack();
	}

	private void broadcastAttack() {
		if (canShout.compareAndSet(true, false)) {
			if (!isDead() && getOwner().getWorldId() == 400030000) {
				switch (getNpcId()) {
					case 297306:
						sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_PORTAL_DEST_69_ATTACKED());
						break;
					case 297307:
						sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_PORTAL_DEST_70_ATTACKED());
						break;
					case 297308:
						sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_PORTAL_DEST_71_ATTACKED());
						break;
					case 297309:
						sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_PORTAL_DEST_72_ATTACKED());
						break;
				}
				allowShout();
			}
		}
	}

	private void allowShout() {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead())
				canShout.set(true);
		}, 3000);
	}

	private void sendPacket(SM_SYSTEM_MESSAGE msg) {
		World.getInstance().getWorldMap(400030000).getMainWorldMapInstance().forEachPlayer(p -> PacketSendUtility.sendPacket(p, msg));
	}

	@Override
	public void handleDied() {
		if (getOwner().getWorldId() == 400030000 && AhserionRaid.getInstance().isStarted()) {
			switch (getNpcId()) {
				case 297306:
					sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_PORTAL_DEST_69_BROKEN());
					break;
				case 297307:
					sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_PORTAL_DEST_70_BROKEN());
					break;
				case 297308:
					sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_PORTAL_DEST_71_BROKEN());
					break;
				case 297309:
					sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_GAB1_SUB_PORTAL_DEST_72_BROKEN());
					break;
			}
			AhserionRaid.getInstance().handleCorridorShieldDestruction(getNpcId());
		}
		RespawnService.scheduleDecayTask(getOwner(), 5000);
		super.handleDied();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_RESPAWN, REWARD_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
