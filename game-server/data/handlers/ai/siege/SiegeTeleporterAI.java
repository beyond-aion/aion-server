package ai.siege;

import java.util.function.Consumer;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORTRESS_INFO;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.GeneralNpcAI;

/**
 * @author Source
 */
@AIName("siege_teleporter")
public class SiegeTeleporterAI extends GeneralNpcAI {

	public SiegeTeleporterAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDespawned() {
		canTeleport(false);
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		canTeleport(false);
		super.handleDied();
	}

	@Override
	protected void handleSpawned() {
		canTeleport(true);
		super.handleSpawned();
	}

	private void canTeleport(final boolean status) {
		final int id = ((SiegeNpc) getOwner()).getSiegeId();

		SiegeService.getInstance().getSiegeLocation(id).setCanTeleport(status);

		getPosition().getWorldMapInstance().forEachPlayer(new Consumer<Player>() {

			@Override
			public void accept(Player player) {
				PacketSendUtility.sendPacket(player, new SM_FORTRESS_INFO(id, status));
			}

		});
	}

}
