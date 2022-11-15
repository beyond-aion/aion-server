package ai.events;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.ChestAI;

/**
 * @author Estrayl
 */
@AIName("hidden_cake")
public class HiddenBirthdayCakeAI extends ChestAI {

	private final static Logger log = LoggerFactory.getLogger("EVENT_LOG");
	private final static AtomicInteger collectedCakes = new AtomicInteger();
	private final static int JEST_SPAWN_CHANCE = 25;
	private final static int[] JEST_SPAWN_IDS = { 210341, 214732, 210595 };
	private static volatile long lastLogTime = System.currentTimeMillis();
	private static volatile int lastCakeCount;

	public HiddenBirthdayCakeAI(Npc owner) {
		super(owner);
	}

	private void logCollectedCakes(int cakes) {
		int deviation = cakes - lastCakeCount;
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastLogTime >= 3600 * 1000) { // Only log once every hour
			log.info("[EVENT] Total cakes collected: {}; Cakes collected during the last hour: {}.", cakes, deviation);
			lastCakeCount = cakes;
			lastLogTime = currentTime;
		}
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (player.isInPlayerMode(PlayerMode.RIDE))
			player.unsetPlayerMode(PlayerMode.RIDE);
		super.handleDialogStart(player);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		if (getOwner().isInState(CreatureState.DEAD))
			return;
		logCollectedCakes(collectedCakes.incrementAndGet());

		if (Rnd.chance() < JEST_SPAWN_CHANCE) {
			Npc npc = (Npc) spawn(Rnd.get(JEST_SPAWN_IDS), getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
			npc.getController().addTask(TaskId.DESPAWN,
				ThreadPoolManager.getInstance().schedule(() -> npc.getController().deleteIfAliveOrCancelRespawn(), 2, TimeUnit.MINUTES));
			PacketSendUtility.sendPacket(player,
				SM_SYSTEM_MESSAGE.STR_MSG_TOYPET_FEED_FOOD_NOT_LOVEFLAVOR(npc.getObjectTemplate().getL10n(), getObjectTemplate().getL10n()));
		}
		super.handleUseItemFinish(player);
	}
}
