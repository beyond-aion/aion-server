package ai.siege;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.stats.container.CreatureLifeStats;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author xTz
 */
@AIName("spring")
public class SpringAI2 extends NpcAI2 {

	@Override
	public void handleSpawned() {
		startSchedule();
	}

	private void startSchedule() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				checkForHeal();
			}
		}, 5000);
	}

	private void checkForHeal() {
		if (!isAlreadyDead() && getPosition().isSpawned()) {
			for (VisibleObject object : getKnownList().getKnownObjects().values()) {
				Creature creature = (Creature) object;
				CreatureLifeStats<?> lifeStats = creature.getLifeStats();
				if (isInRange(creature, 10) && !creature.getEffectController().hasAbnormalEffect(19116)
					&& !lifeStats.isAlreadyDead() && (lifeStats.getCurrentHp() < lifeStats.getMaxHp()))
					if (creature instanceof SiegeNpc) {
						SiegeNpc npc = (SiegeNpc) creature;
						if (getObjectTemplate().getRace() == npc.getObjectTemplate().getRace()) {
							doHeal();
							break;
						}
					}
					else if (creature instanceof Player) {
						Player player = (Player) creature;
						if (getObjectTemplate().getRace() == player.getRace() && player.isOnline()) {
							doHeal();
							break;
						}
					}
			}
			startSchedule();
		}
	}

	private void doHeal() {
		AI2Actions.targetSelf(this);
		AI2Actions.useSkill(this, 19116);
	}
}
