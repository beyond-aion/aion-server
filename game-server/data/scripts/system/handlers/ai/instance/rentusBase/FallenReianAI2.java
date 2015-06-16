package ai.instance.rentusBase;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.MathUtil;

/**
 *
 * @author xTz
 */
@AIName("fallen_reian")
public class FallenReianAI2 extends NpcAI2 {

private AtomicBoolean isCollapsed = new AtomicBoolean(false);

	private int doorId;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		doorId = getNpcId() == 799661 ? 16 : 54;
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature instanceof Player) {
			final Player player = (Player) creature;
			if (MathUtil.getDistance(getOwner(), player) <= doorId) {
				if (MathUtil.getDistance(getOwner(), getPosition().getWorldMapInstance().getDoors().get(doorId)) <= 30) {
					if (isCollapsed.compareAndSet(false, true)) {
						getPosition().getWorldMapInstance().getDoors().get(doorId).setOpen(true);
					}
				}
			}
		}
	}

}