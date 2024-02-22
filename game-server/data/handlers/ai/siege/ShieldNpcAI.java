package ai.siege;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.Race;
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
	public boolean canThink() {
		// prevent field stone from resetting
		return getOwner().getRace() != Race.CONSTRUCT;
	}

	@Override
	protected void handleDespawned() {
		updateFortressShieldStatus(false);
		super.handleDespawned();
	}

	@Override
	protected void handleSpawned() {
		updateFortressShieldStatus(true);
		super.handleSpawned();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_AP_XP_DP_LOOT, REWARD_AP -> true;
			case REWARD_LOOT, ALLOW_RESPAWN -> false;
			default -> super.ask(question);
		};
	}

	private void updateFortressShieldStatus(boolean hasShield) {
		int siegeLocationId = getSpawnTemplate().getSiegeId();
		SiegeService.getInstance().getFortress(siegeLocationId).setUnderShield(hasShield);
		PacketSendUtility.broadcastToMap(getPosition().getWorldMapInstance(), new SM_SHIELD_EFFECT(siegeLocationId));
	}

}
