package ai.siege;

import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.siege.Siege;

public abstract class AbstractSiegeProtectorAI extends SiegeNpcAI {

	public AbstractSiegeProtectorAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleBackHome() {
		super.handleBackHome();
		getAggroList().clear(); // make sure old damages aren't counted in stopSiege
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		stopSiege();
	}

	private void stopSiege() {
		Siege<? extends SiegeLocation> siege = getSiege();
		for (AggroInfo aggroInfo : getAggroList().getList()) {
			if (aggroInfo.getAttacker() instanceof Creature attacker)
				siege.getSiegeCounter().addDamage(attacker.getMaster(), aggroInfo.getDamage());
		}
		siege.setBossKilled(true);
		SiegeService.getInstance().stopSiege(siege.getSiegeLocationId());
	}
}
