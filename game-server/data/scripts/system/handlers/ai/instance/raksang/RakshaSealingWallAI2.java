package ai.instance.raksang;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.GeneralNpcAI2;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
@AIName("raksha_sealing_wall")
public class RakshaSealingWallAI2 extends GeneralNpcAI2 {

	private AtomicBoolean startedEvent = new AtomicBoolean(false);

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature instanceof Player) {
			final Player player = (Player) creature;
			if (MathUtil.getDistance(getOwner(), player) <= 35) {
				if (startedEvent.compareAndSet(false, true)) {
					WorldMapInstance instance = getPosition().getWorldMapInstance();
					Npc sharik = instance.getNpc(217425);
					Npc flamelord = instance.getNpc(217451);
					Npc sealguard = instance.getNpc(217456);
					int bossId;
					if ((sharik == null || NpcActions.isAlreadyDead(sharik)) && (flamelord == null || NpcActions.isAlreadyDead(flamelord))
						&& (sealguard == null || NpcActions.isAlreadyDead(sealguard))) {
						bossId = 217475;
					} else {
						bossId = 217647;
					}
					spawn(bossId, 1063.08f, 903.13f, 138.744f, (byte) 29);
					AI2Actions.deleteOwner(this);
				}
			}
		}
	}

}
