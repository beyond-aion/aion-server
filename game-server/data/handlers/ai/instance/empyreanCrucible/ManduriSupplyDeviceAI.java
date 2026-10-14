package ai.instance.empyreanCrucible;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.ChatType;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * Feed and water supply devices of stage 5. They work only while the giant fights and only when two players pull at the same time: the second
 * one to start pulling puts out the feed or water, a player pulling alone gives up when the bar runs out.
 */
@AIName("empyrean_manduri_supply_device")
public class ManduriSupplyDeviceAI extends ActionItemNpcAI {

	private final Map<Integer, Long> pulling = new ConcurrentHashMap<>();
	private final AtomicBoolean used = new AtomicBoolean();

	public ManduriSupplyDeviceAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		Npc giant = getPosition().getWorldMapInstance().getNpc(217553);
		if (giant == null || !(giant.getAi() instanceof ManduriGiantAI ai) || !ai.isInFight())
			return;
		super.handleDialogStart(player);
	}

	@Override
	protected void handleUseItemStart(Player player) {
		long now = System.currentTimeMillis();
		pulling.values().removeIf(start -> now - start > getTalkDelayInMs() + 500);
		boolean helped = !pulling.isEmpty() && !pulling.containsKey(player.getObjectId());
		if (helped) {
			if (used.compareAndSet(false, true)) {
				say(player, 1111304); // Yay, it worked!
				AIActions.handleUseItemFinish(this, player);
			}
			return;
		}
		pulling.put(player.getObjectId(), now);
		say(player, 1111303); // Heave-ho...
		super.handleUseItemStart(player);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		pulling.remove(player.getObjectId());
		if (!used.get())
			say(player, 1111305); // Too much for one, it takes another one to push it!
	}

	/**
	 * The pulling player says it, not the device.
	 */
	private void say(Player player, int msgId) {
		PacketSendUtility.broadcastPacket(player, new SM_SYSTEM_MESSAGE(ChatType.NPC, player, msgId), true);
	}
}
