package ai.instance.drakenspire;

import ai.AggressiveNoLootNpcAI;
import ai.AggressiveNpcAI;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;

import java.util.Comparator;
import java.util.concurrent.Future;

/**
 *
 * @author Estrayl
 */
@AIName("drakenspire_dragon_beritra")
public class DragonBeritraAI extends AggressiveNoLootNpcAI {

	private Future<?> authorityTask;

	public DragonBeritraAI(Npc owner) {
		super(owner);
	}

	/**
	 * Part of the de-spawn sequence:
	 * => Spawn soul extinction fields on all players in descending order i.e., starting with the player, which dealt the most damage.
	 */
	private void wipe() {
		getAggroList().getList().stream().filter(ai -> ai.getAttacker() instanceof Player).sorted(
			Comparator.comparingInt(AggroInfo::getDamage).reversed()).forEach(ai -> {
			Player p = (Player) ai.getAttacker();
			spawn(855450, p.getX(), p.getY(), p.getZ(), (byte) 0);
		});
		// STR_CHAT_IDSeal_Vritra_Human_Gossip_09
		PacketSendUtility.broadcastMessage(getOwner(), 1501276);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case CONSIDER_BOUNDS_IN_CAN_SEE_CHECK_WHEN_ATTACKED, CONSIDER_BOUNDS_IN_CAN_SEE_CHECK_WHEN_ATTACKING -> true;
			default -> super.ask(question);
		};
	}
}
