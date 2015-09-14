package ai.instance.RukibukiCircusTroupe;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.knownlist.Visitor;

/**
 * @author Ritsu
 */
@AIName("solidironchain")
public class SolidIronChainAI2 extends AggressiveNpcAI2 {

	@Override
	public boolean canThink() {
		return false;
	}

	private AtomicBoolean moviePlayed = new AtomicBoolean();

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		if (moviePlayed.compareAndSet(false, true)) {
			getPosition().getWorldMapInstance().doOnAllPlayers(new Visitor<Player>() {

				@Override
				public void visit(Player p) {
					PacketSendUtility.sendPacket(p, new SM_PLAY_MOVIE(0, 983));
				}

			});
		}
	}

}
