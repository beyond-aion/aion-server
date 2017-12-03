package ai.instance.raksang;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.GeneralNpcAI;

/**
 * @author xTz
 */
@AIName("raksha_sealing_wall")
public class RakshaSealingWallAI extends GeneralNpcAI {

	private AtomicBoolean startedEvent = new AtomicBoolean(false);

	public RakshaSealingWallAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature instanceof Player) {
			final Player player = (Player) creature;
			if (PositionUtil.getDistance(getOwner(), player) <= 35) {
				if (startedEvent.compareAndSet(false, true)) {
					WorldMapInstance instance = getPosition().getWorldMapInstance();
					Npc sharik = instance.getNpc(217425);
					Npc flamelord = instance.getNpc(217451);
					Npc sealguard = instance.getNpc(217456);
					int bossId;
					if ((sharik == null || sharik.isDead()) && (flamelord == null || flamelord.isDead()) && (sealguard == null || sealguard.isDead())) {
						bossId = 217475;
					} else {
						bossId = 217647;
					}
					spawn(bossId, 1063.08f, 903.13f, 138.744f, (byte) 29);
					AIActions.deleteOwner(this);
				}
			}
		}
	}

}
