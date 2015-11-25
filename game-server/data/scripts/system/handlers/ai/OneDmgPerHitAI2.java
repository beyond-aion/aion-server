package ai;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.NpcGameStats;

/**
 * @author xTz, Neon
 */
@AIName("onedmg_passive")
public class OneDmgPerHitAI2 extends NoActionAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		disableMissingHits();
	}

	@Override
	protected void handleRespawned() {
		super.handleRespawned();
		disableMissingHits();
	}

	@Override
	public int modifyDamage(Creature creature, int damage) {
		return 1;
	}

	private void disableMissingHits() {
		NpcGameStats stats = getOwner().getGameStats();
		Stat2 mRes = stats.getMResist();
		Stat2 evasion = stats.getEvasion();
		mRes.setBase(0);
		mRes.setBonus(0);
		evasion.setBase(0);
		evasion.setBonus(0);
	}
}
