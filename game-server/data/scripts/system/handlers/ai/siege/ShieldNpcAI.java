package ai.siege;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHIELD_EFFECT;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Source
 */
@AIName("siege_shieldnpc")
public class ShieldNpcAI extends SiegeNpcAI {

	public ShieldNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDespawned() {
		sendShieldPacket(false);
		super.handleDespawned();
	}

	@Override
	protected void handleSpawned() {
		sendShieldPacket(true);
		super.handleSpawned();
	}
	
	@Override
	protected void handleAttack(Creature creature) {
		switch (getOwner().getRace()) {
			case CONSTRUCT:
				getOwner().getController().loseAggro(false);
		}
	}

	private void sendShieldPacket(boolean shieldStatus) {
		int id = getSpawnTemplate().getSiegeId();
		SiegeService.getInstance().getFortress(id).setUnderShield(shieldStatus);

		final SM_SHIELD_EFFECT packet = new SM_SHIELD_EFFECT(id);
		getPosition().getWorldMapInstance().forEachPlayer(p -> PacketSendUtility.sendPacket(p, packet));
	}

}
